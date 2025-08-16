package com.example.pixel.execution;

import com.example.pixel.node.Node;
import com.example.pixel.node.NodeProcessorService;
import com.example.pixel.common.NotificationService;
import com.example.pixel.common.PerformanceTracker;
import com.example.pixel.task.TaskPayload;
import com.example.pixel.task.TaskService;
import com.example.pixel.task.TaskStatus;
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
public class ExecutionService {

    private final NodeProcessorService nodeProcessorService;
    private final PerformanceTracker performanceTracker;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public TaskPayload startExecution(Graph graph, Long sceneId) {
        log.info("startGraphExecution: Creating task for sceneId={} ...", sceneId);
        TaskPayload task = taskService.createTask(graph, sceneId);
        Long taskId = task.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", taskId);
        CompletableFuture.runAsync(() -> execute(graph, taskId, sceneId), graphTaskExecutor);
        return task;
    }

    public CompletableFuture<TaskPayload> startExecutionAsync(Graph graph, Long sceneId) {
        log.info("startGraphExecutionAsync: Creating task for sceneId={} ...", sceneId);
        TaskPayload task = taskService.createTask(graph, sceneId);
        log.info("startGraphExecutionAsync: Task created with id={}, starting graph execution ...", task.getId());
        return CompletableFuture.supplyAsync(() -> execute(graph, task.getId(), sceneId).join(), graphTaskExecutor);
    }

    public CompletableFuture<TaskPayload> startExecutionSync(Graph graph, Long sceneId) {
        log.info("startGraphExecutionSync: Creating task for sceneId={} ...", sceneId);
        TaskPayload task = taskService.createTask(graph, sceneId);
        log.info("startGraphExecutionSync: Task created with id={}, starting graph execution ...", task.getId());
        return execute(graph, task.getId(), sceneId);
    }

    private CompletableFuture<TaskPayload> execute(Graph graph, Long taskId, Long sceneId) {
        log.info("executeGraph: Called for taskId={}, sceneId={}", taskId, sceneId);
        return performanceTracker.trackOperation(
                "graph.execution",
                Tags.of("scene.id", String.valueOf(sceneId)),
                () -> executeInternal(graph, taskId, sceneId)
        );
    }

    private CompletableFuture<TaskPayload> executeInternal(Graph graph, Long taskId, Long sceneId) {
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
                notificationService.sendTaskStatus(taskService.findTaskById(taskId));

                log.debug("Node with id: {} is processed (taskId={})", node.getId(), taskId);
            }

            log.info("All nodes processed for taskId={}, updating status to COMPLETED", taskId);
            taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
            notificationService.sendTaskStatus(taskService.findTaskById(taskId));

            return CompletableFuture.completedFuture(taskService.findTaskById(taskId));
        } catch (Exception e) {
            log.error("Error processing graph for scene {} (taskId={}): {}", sceneId, taskId, e.getMessage(), e);

            taskService.markTaskFailed(taskId, e.getMessage());
            notificationService.sendTaskStatus(taskService.findTaskById(taskId));

            CompletableFuture<TaskPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}