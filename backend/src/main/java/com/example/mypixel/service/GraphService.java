package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.model.node.Node;
import io.micrometer.core.instrument.Tags;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
@Service
@Slf4j
public class GraphService {

    private final NodeProcessorService nodeProcessorService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PerformanceTracker performanceTracker;
    private final TaskService taskService;

    @Transactional
    public GraphExecutionTask startGraphExecution(Graph graph, Long sceneId) {
        log.debug("Starting execution for scene {}", sceneId);

        GraphExecutionTask task = taskService.createTask(graph, sceneId);
        executeGraph(graph, task, sceneId);

        return task;
    }

    @Async("graphTaskExecutor")
    public CompletableFuture<GraphExecutionTask> executeGraph(
            Graph graph,
            GraphExecutionTask task,
            Long sceneId
    ) {
        return performanceTracker.trackOperation(
                "graph.execution",
                Tags.of("scene.id", String.valueOf(sceneId)),
                () -> executeGraphInternal(graph, task, sceneId)
        );
    }

    private CompletableFuture<GraphExecutionTask> executeGraphInternal(
            Graph graph,
            GraphExecutionTask task,
            Long sceneId
    ) {
        try {
            taskService.updateTaskStatus(task, TaskStatus.RUNNING);

            Iterator<Node> iterator = graph.iterator();
            int totalNodes = graph.getNodes().size();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                Node node = iterator.next();

                Tags nodeTags = Tags.of(
                        "node.id", String.valueOf(node.getId()),
                        "node.type", node.getType(),
                        "scene.id", String.valueOf(sceneId),
                        "task.id", String.valueOf(task.getId())
                );

                performanceTracker.trackOperation(
                        "node.execution",
                        nodeTags,
                        () -> nodeProcessorService.processNode(node, sceneId, task.getId())
                );

                processedNodes++;

                taskService.updateTaskProgress(task, processedNodes);
                sendProgressWebSocket(sceneId, processedNodes, totalNodes);

                log.debug("Node with id: {} is processed", node.getId());
            }

            taskService.updateTaskStatus(task, TaskStatus.COMPLETED);
            sendCompletedWebSocket(sceneId);

            return CompletableFuture.completedFuture(task);

        } catch (Exception e) {
            log.error("Error processing graph for scene {}: {}", sceneId, e.getMessage(), e);

            taskService.markTaskFailed(task, e.getMessage());
            sendErrorWebSocket(sceneId, e.getMessage());

            throw e;
        }
    }

    private void sendProgressWebSocket(Long sceneId, int processed, int total) {
        try {
            int percent = total > 0 ? (int) Math.round((double) processed / total * 100) : 0;

            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "in_progress");
            payload.put("processedNodes", processed);
            payload.put("totalNodes", total);
            payload.put("progressPercent", percent);
            payload.put("message", String.format("Progress: %d/%d nodes processed (%d%%)",
                    processed, total, percent));

            messagingTemplate.convertAndSend("/topic/processing/" + sceneId, payload);
            log.debug("Sent progress WebSocket: {}/{} ({}%)", processed, total, percent);
        } catch (Exception e) {
            log.error("Failed to send progress WebSocket: {}", e.getMessage());
        }
    }

    private void sendCompletedWebSocket(Long sceneId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "completed");
            payload.put("message", "Completed");

            messagingTemplate.convertAndSend("/topic/processing/" + sceneId, payload);
            log.debug("Sent completed WebSocket");
        } catch (Exception e) {
            log.error("Failed to send completed WebSocket: {}", e.getMessage());
        }
    }

    private void sendErrorWebSocket(Long sceneId, String errorMessage) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "failed");
            payload.put("message", "Failed: " + errorMessage);

            messagingTemplate.convertAndSend("/topic/processing/" + sceneId, payload);
            log.debug("Sent error WebSocket");
        } catch (Exception e) {
            log.error("Failed to send error WebSocket: {}", e.getMessage());
        }
    }
}
