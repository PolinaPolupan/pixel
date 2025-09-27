package com.example.pixel.graph_execution.executor;

import com.example.pixel.graph.model.Graph;
import com.example.pixel.node.model.Node;
import com.example.pixel.node.service.NodeProcessorService;
import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Slf4j
@RequiredArgsConstructor
@Component
public class GraphExecutor {

    private final NodeProcessorService nodeProcessorService;
    private final GraphExecutionService graphExecutionService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public GraphExecutionPayload startExecution(Graph graph) {
        GraphExecutionPayload task = graphExecutionService.create(graph);
        Long taskId = task.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", taskId);
        CompletableFuture.runAsync(() -> execute(graph, taskId), graphTaskExecutor);
        return task;
    }

    public CompletableFuture<GraphExecutionPayload> startExecutionAsync(Graph graph) {
        GraphExecutionPayload task = graphExecutionService.create(graph);
        log.info("startGraphExecutionAsync: Task created with id={}, starting graph execution ...", task.getId());
        return CompletableFuture.supplyAsync(() -> execute(graph, task.getId()).join(), graphTaskExecutor);
    }

    private CompletableFuture<GraphExecutionPayload> execute(Graph graph, Long taskId) {
        try {
            log.debug("Updating task status to RUNNING for taskId={}", taskId);
            graphExecutionService.updateStatus(taskId, GraphExecutionStatus.RUNNING);

            Iterator<Node> iterator = graph.iterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                Node node = iterator.next();
                log.debug("Processing node id={} for taskId={}", node.getId(), taskId);

                nodeProcessorService.processNode(node, graph.getId(), taskId);

                processedNodes++;
                log.debug("Node processed, updating progress: processedNodes={}/{}", processedNodes, graph.getNodes().size());

                graphExecutionService.updateProgress(taskId, processedNodes);
                notificationService.sendTaskStatus(graphExecutionService.findById(taskId));

                log.debug("Node with id: {} is processed (taskId={})", node.getId(), taskId);
            }

            log.info("All nodes processed for taskId={}, updating status to COMPLETED", taskId);
            graphExecutionService.updateStatus(taskId, GraphExecutionStatus.COMPLETED);
            notificationService.sendTaskStatus(graphExecutionService.findById(taskId));

            return CompletableFuture.completedFuture(graphExecutionService.findById(taskId));
        } catch (Exception e) {
            log.error("Error processing graph with id {} (taskId={}): {}", graph.getId(), taskId, e.getMessage(), e);

            graphExecutionService.markFailed(taskId, e.getMessage());
            notificationService.sendTaskStatus(graphExecutionService.findById(taskId));

            CompletableFuture<GraphExecutionPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}