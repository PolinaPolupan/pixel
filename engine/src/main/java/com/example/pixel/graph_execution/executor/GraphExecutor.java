package com.example.pixel.graph_execution.executor;

import com.example.pixel.graph.model.Graph;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.node.model.Node;
import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import com.example.pixel.node_execution.service.NodeExecutionService;
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

    private final NodeExecutionService nodeExecutionService;
    private final GraphExecutionService graphExecutionService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public GraphExecutionPayload startExecution(Graph graph) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionService.create(graph);
        Long graphExecutionId = graphExecutionEntity.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", graphExecutionId);
        CompletableFuture.runAsync(() -> execute(graph, graphExecutionId), graphTaskExecutor);
        return GraphExecutionPayload.fromEntity(graphExecutionEntity);
    }

    private CompletableFuture<GraphExecutionPayload> execute(Graph graph, Long graphExecutionId) {
        try {
            log.debug("Updating task status to RUNNING for graphExecutionId={}", graphExecutionId);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.RUNNING);

            Iterator<Node> iterator = graph.iterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                Node node = iterator.next();
                log.debug("Processing node id={} for graphExecutionId={}", node.getId(), graphExecutionId);

                nodeExecutionService.execute(node, graphExecutionId);

                processedNodes++;
                log.debug("Node processed, updating progress: processedNodes={}/{}", processedNodes, graph.getNodes().size());

                graphExecutionService.updateProgress(graphExecutionId, processedNodes);
                notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

                log.debug("Node with id: {} is processed (graphExecutionId={})", node.getId(), graphExecutionId);
            }

            log.info("All nodes processed for graphExecutionId={}, updating status to COMPLETED", graphExecutionId);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.COMPLETED);
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            return CompletableFuture.completedFuture(graphExecutionService.findById(graphExecutionId));
        } catch (Exception e) {
            log.error("Error processing graph with id {} (graphExecutionId={}): {}", graph.getId(), graphExecutionId, e.getMessage(), e);

            graphExecutionService.markFailed(graphExecutionId, e.getMessage());
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            CompletableFuture<GraphExecutionPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}