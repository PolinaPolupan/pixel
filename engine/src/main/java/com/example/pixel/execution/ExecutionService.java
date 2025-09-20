package com.example.pixel.execution;

import com.example.pixel.node.Node;
import com.example.pixel.node.NodeProcessorService;
import com.example.pixel.common.NotificationService;
import com.example.pixel.execution_task.ExecutionTaskPayload;
import com.example.pixel.execution_task.ExecutionTaskService;
import com.example.pixel.execution_task.ExecutionTaskStatus;
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
    private final ExecutionTaskService executionTaskService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public ExecutionTaskPayload startExecution(ExecutionGraphPayload executionGraphPayload, Long sceneId) {
        ExecutionGraph executionGraph = executionGraphPayload.toExecutionGraph();
        log.info("startGraphExecution: Creating task for sceneId={} ...", sceneId);
        ExecutionTaskPayload task = executionTaskService.createTask(executionGraph, sceneId);
        Long taskId = task.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", taskId);
        CompletableFuture.runAsync(() -> execute(executionGraph, taskId, sceneId), graphTaskExecutor);
        return task;
    }

    public CompletableFuture<ExecutionTaskPayload> startExecutionAsync(ExecutionGraphPayload executionGraphPayload, Long sceneId) {
        ExecutionGraph executionGraph = executionGraphPayload.toExecutionGraph();
        log.info("startGraphExecutionAsync: Creating task for sceneId={} ...", sceneId);
        ExecutionTaskPayload task = executionTaskService.createTask(executionGraph, sceneId);
        log.info("startGraphExecutionAsync: Task created with id={}, starting graph execution ...", task.getId());
        return CompletableFuture.supplyAsync(() -> execute(executionGraph, task.getId(), sceneId).join(), graphTaskExecutor);
    }

    public CompletableFuture<ExecutionTaskPayload> startExecutionSync(ExecutionGraphPayload executionGraphPayload, Long sceneId) {
        ExecutionGraph executionGraph = executionGraphPayload.toExecutionGraph();
        log.info("startGraphExecutionSync: Creating task for sceneId={} ...", sceneId);
        ExecutionTaskPayload task = executionTaskService.createTask(executionGraph, sceneId);
        log.info("startGraphExecutionSync: Task created with id={}, starting graph execution ...", task.getId());
        return execute(executionGraph, task.getId(), sceneId);
    }

    private CompletableFuture<ExecutionTaskPayload> execute(ExecutionGraph executionGraph, Long taskId, Long sceneId) {
        log.info("executeGraphInternal: Starting execution for taskId={}, sceneId={}", taskId, sceneId);
        try {
            log.debug("Updating task status to RUNNING for taskId={}", taskId);
            executionTaskService.updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);

            Iterator<Node> iterator = executionGraph.iterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                Node node = iterator.next();
                log.debug("Processing node id={} for taskId={}", node.getId(), taskId);

                nodeProcessorService.processNode(node, sceneId, taskId);

                processedNodes++;
                log.debug("Node processed, updating progress: processedNodes={}/{}", processedNodes, executionGraph.getNodes().size());

                executionTaskService.updateTaskProgress(taskId, processedNodes);
                notificationService.sendTaskStatus(executionTaskService.findTaskById(taskId));

                log.debug("Node with id: {} is processed (taskId={})", node.getId(), taskId);
            }

            log.info("All nodes processed for taskId={}, updating status to COMPLETED", taskId);
            executionTaskService.updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
            notificationService.sendTaskStatus(executionTaskService.findTaskById(taskId));

            return CompletableFuture.completedFuture(executionTaskService.findTaskById(taskId));
        } catch (Exception e) {
            log.error("Error processing graph for scene {} (taskId={}): {}", sceneId, taskId, e.getMessage(), e);

            executionTaskService.markTaskFailed(taskId, e.getMessage());
            notificationService.sendTaskStatus(executionTaskService.findTaskById(taskId));

            CompletableFuture<ExecutionTaskPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}