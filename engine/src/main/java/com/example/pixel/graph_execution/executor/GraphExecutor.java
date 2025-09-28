package com.example.pixel.graph_execution.executor;

import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.model.Graph;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.node_execution.model.NodeExecution;
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

    public GraphExecutionPayload startExecution(GraphPayload graphPayload) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionService.create(graphPayload);
        Long graphExecutionId = graphExecutionEntity.getId();
        log.info("startGraphExecution: Task created with id={}, launching async graph execution ...", graphExecutionId);
        CompletableFuture.runAsync(() -> execute(graphPayload, graphExecutionId), graphTaskExecutor);
        return GraphExecutionPayload.fromEntity(graphExecutionEntity);
    }

    private CompletableFuture<GraphExecutionPayload> execute(GraphPayload graphPayload, Long graphExecutionId) {
        try {
            Graph graph = new Graph(graphPayload.getId(), graphPayload.getNodeExecutions());
            log.debug("Updating task status to RUNNING for graphExecutionId={}", graphExecutionId);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.RUNNING);

            Iterator<NodeExecution> iterator = graph.iterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                NodeExecution nodeExecution = iterator.next();
                log.debug("Processing node id={} for graphExecutionId={}", nodeExecution.getId(), graphExecutionId);

                nodeExecutionService.execute(nodeExecution, graphExecutionId);

                processedNodes++;
                log.debug("Node processed, updating progress: processedNodes={}/{}", processedNodes, graph.getNodeExecutions().size());

                graphExecutionService.updateProgress(graphExecutionId, processedNodes);
                notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

                log.debug("Node with id: {} is processed (graphExecutionId={})", nodeExecution.getId(), graphExecutionId);
            }

            log.info("All nodes processed for graphExecutionId={}, updating status to COMPLETED", graphExecutionId);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.COMPLETED);
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            return CompletableFuture.completedFuture(graphExecutionService.findById(graphExecutionId));
        } catch (Exception e) {
            log.error("Error processing graph with id {} (graphExecutionId={}): {}", graphPayload.getId(), graphExecutionId, e.getMessage(), e);

            graphExecutionService.markFailed(graphExecutionId, e.getMessage());
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            CompletableFuture<GraphExecutionPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}