package com.example.pixel.execution_graph.service;

import com.example.pixel.execution_graph.model.ExecutionGraph;
import com.example.pixel.node.model.Node;
import com.example.pixel.node.service.NodeProcessorService;
import com.example.pixel.common.service.NotificationService;
import com.example.pixel.execution_task.model.ExecutionTaskPayload;
import com.example.pixel.execution_task.service.ExecutionTaskService;
import com.example.pixel.execution_task.model.ExecutionTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Service
@Slf4j
public class GraphExecutor {

    private final NodeProcessorService nodeProcessorService;
    private final ExecutionTaskService executionTaskService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public ExecutionTaskPayload startExecution(ExecutionGraph executionGraph) {
        ExecutionTaskPayload task = executionTaskService.createTask(executionGraph);
        Long taskId = task.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", taskId);
        CompletableFuture.runAsync(() -> execute(executionGraph, taskId), graphTaskExecutor);
        return task;
    }

    public CompletableFuture<ExecutionTaskPayload> startExecutionAsync(ExecutionGraph executionGraph) {
        ExecutionTaskPayload task = executionTaskService.createTask(executionGraph);
        log.info("startGraphExecutionAsync: Task created with id={}, starting graph execution ...", task.getId());
        return CompletableFuture.supplyAsync(() -> execute(executionGraph, task.getId()).join(), graphTaskExecutor);
    }

    private CompletableFuture<ExecutionTaskPayload> execute(ExecutionGraph executionGraph, Long taskId) {
        try {
            log.debug("Updating task status to RUNNING for taskId={}", taskId);
            executionTaskService.updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);

            Iterator<Node> iterator = executionGraph.iterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                Node node = iterator.next();
                log.debug("Processing node id={} for taskId={}", node.getId(), taskId);

                nodeProcessorService.processNode(node, executionGraph.getId(), taskId);

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
            log.error("Error processing graph with id {} (taskId={}): {}", executionGraph.getId(), taskId, e.getMessage(), e);

            executionTaskService.markTaskFailed(taskId, e.getMessage());
            notificationService.sendTaskStatus(executionTaskService.findTaskById(taskId));

            CompletableFuture<ExecutionTaskPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}