package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.model.node.Node;
import io.micrometer.core.instrument.Tags;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
@Service
@Slf4j
public class GraphService {

    private final NodeProcessorService nodeProcessorService;
    private final PerformanceTracker performanceTracker;
    private final TaskService taskService;
    private final NotificationService notificationService;

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
                notificationService.sendProgress(sceneId, processedNodes, totalNodes);

                log.debug("Node with id: {} is processed", node.getId());
            }

            taskService.updateTaskStatus(task, TaskStatus.COMPLETED);
            notificationService.sendCompleted(sceneId);

            return CompletableFuture.completedFuture(task);
        } catch (Exception e) {
            log.error("Error processing graph for scene {}: {}", sceneId, e.getMessage(), e);

            taskService.markTaskFailed(task, e.getMessage());
            notificationService.sendError(sceneId, e.getMessage());

            throw e;
        }
    }
}
