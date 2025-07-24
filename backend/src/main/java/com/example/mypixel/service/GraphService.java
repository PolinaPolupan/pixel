package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Task;
import com.example.mypixel.model.TaskPayload;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.model.node.Node;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Service
@Slf4j
public class GraphService {

    private final NodeProcessorService nodeProcessorService;
    private final PerformanceTracker performanceTracker;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public TaskPayload startGraphExecution(Graph graph, Long sceneId) {
        log.info("startGraphExecution: Creating task for sceneId={} ...", sceneId);
        Task task = taskService.createTask(graph, sceneId);
        Long taskId = task.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", taskId);
        CompletableFuture.runAsync(() -> executeGraph(graph, taskId, sceneId), graphTaskExecutor);
        return TaskPayload.fromEntity(task);
    }

    public CompletableFuture<TaskPayload> startGraphExecutionAsync(Graph graph, Long sceneId) {
        log.info("startGraphExecutionAsync: Creating task for sceneId={} ...", sceneId);
        Task task = taskService.createTask(graph, sceneId);
        log.info("startGraphExecutionAsync: Task created with id={}, starting graph execution ...", task.getId());
        return CompletableFuture.supplyAsync(() -> executeGraph(graph, task.getId(), sceneId).join(), graphTaskExecutor);
    }

    public CompletableFuture<TaskPayload> startGraphExecutionSync(Graph graph, Long sceneId) {
        log.info("startGraphExecutionSync: Creating task for sceneId={} ...", sceneId);
        Task task = taskService.createTask(graph, sceneId);
        log.info("startGraphExecutionSync: Task created with id={}, starting graph execution ...", task.getId());
        return executeGraph(graph, task.getId(), sceneId);
    }

    public CompletableFuture<TaskPayload> executeGraph(
            Graph graph,
            Long taskId,
            Long sceneId
    ) {
        log.info("executeGraph: Called for taskId={}, sceneId={}", taskId, sceneId);
        return performanceTracker.trackOperation(
                "graph.execution",
                Tags.of("scene.id", String.valueOf(sceneId)),
                () -> executeGraphInternal(graph, taskId, sceneId)
        );
    }

    private CompletableFuture<TaskPayload> executeGraphInternal(
            Graph graph,
            Long taskId,
            Long sceneId
    ) {
        log.info("executeGraphInternal: Starting execution for taskId={}, sceneId={}", taskId, sceneId);
        try {
            log.debug("Updating task status to RUNNING for taskId={}", taskId);
            taskService.updateTaskStatus(taskId, TaskStatus.RUNNING);

            Iterator<Node> iterator = graph.iterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                Node node = iterator.next();
                log.debug("Processing node id={} for taskId={}", node.getId(), taskId);

                nodeProcessorService.processNode(node, sceneId, taskId);

                processedNodes++;
                log.debug("Node processed, updating progress: processedNodes={}/{}", processedNodes, graph.getNodes().size());

                taskService.updateTaskProgress(taskId, processedNodes);
                notificationService.sendTaskStatus(TaskPayload.fromEntity(taskService.findTaskById(taskId)));

                log.debug("Node with id: {} is processed (taskId={})", node.getId(), taskId);
            }

            log.info("All nodes processed for taskId={}, updating status to COMPLETED", taskId);
            taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
            notificationService.sendTaskStatus(TaskPayload.fromEntity(taskService.findTaskById(taskId)));

            return CompletableFuture.completedFuture(TaskPayload.fromEntity(taskService.findTaskById(taskId)));
        } catch (Exception e) {
            log.error("Error processing graph for scene {} (taskId={}): {}", sceneId, taskId, e.getMessage(), e);

            try {
                taskService.markTaskFailed(taskId, e.getMessage());
                notificationService.sendTaskStatus(TaskPayload.fromEntity(taskService.findTaskById(taskId)));
            } catch (Exception inner) {
                log.error("Failed to mark task as failed or send notification for taskId={}: {}", taskId, inner.getMessage(), inner);
            }

            CompletableFuture<TaskPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}