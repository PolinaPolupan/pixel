package com.example.pixel.graph_execution.executor;

import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.model.Graph;
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
import java.util.List;
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

    public void startExecution(GraphPayload graphPayload, GraphExecutionPayload graphExecutionPayload) {
        log.info("startGraphExecution created with id={}, launching async graph execution ...", graphExecutionPayload.getId());
        CompletableFuture.runAsync(() -> execute(graphPayload, graphExecutionPayload), graphTaskExecutor);
    }

    private void execute(GraphPayload graphPayload, GraphExecutionPayload graphExecutionPayload) {
        Long graphExecutionId = graphExecutionPayload.getId();
        try {
            Graph graph = new Graph(graphPayload.getNodes());
            log.debug("Updating task status to RUNNING for graphExecutionId={}", graphExecutionId);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.RUNNING);

            Iterator<List<NodeExecution>> levelIterator = graph.levelIterator();
            int processedNodes = 0;

            while (levelIterator.hasNext()) {
                List<NodeExecution> batch = levelIterator.next();
                log.info("[GraphExecutionId={}] Starting new level with {} nodes", graphExecutionId, batch.size());

                List<CompletableFuture<Void>> futures = batch.stream()
                        .map(node -> nodeExecutionService.executeAsync(node, graphExecutionId))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                processedNodes += batch.size();
                log.info("[GraphExecutionId={}] Completed level. ProcessedNodes={}/{}",
                        graphExecutionId, processedNodes, processedNodes);

                graphExecutionService.updateProgress(graphExecutionId, processedNodes);
                notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));
            }

            log.info("All nodes processed for graphExecutionId={}, updating status to COMPLETED", graphExecutionId);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.COMPLETED);
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            CompletableFuture.completedFuture(graphExecutionPayload);
        } catch (Exception e) {
            log.error("Error processing graph with id {} (graphExecutionId={}): {}", graphPayload.getId(), graphExecutionId, e.getMessage(), e);

            graphExecutionService.markFailed(graphExecutionId, e.getMessage());
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            CompletableFuture<GraphExecutionPayload> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
        }
    }
}