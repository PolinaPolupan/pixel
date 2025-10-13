package com.example.pixel.graph_execution.executor;

import com.example.pixel.common.exception.GraphExecutionException;
import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.model.Graph;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import com.example.pixel.node_execution.executor.NodeExecutor;
import com.example.pixel.node_execution.model.NodeExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
public class AsyncGraphExecutor implements GraphExecutor {

    private final NodeExecutor nodeExecutor;
    private final GraphExecutionService graphExecutionService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public CompletableFuture<GraphExecutionPayload> launchExecution(GraphPayload graphPayload, GraphExecutionPayload graphExecutionPayload) {
        log.info("startGraphExecution created with id={}, launching async graph execution ...", graphExecutionPayload.getId());
        return CompletableFuture.supplyAsync(() -> (execute(graphPayload, graphExecutionPayload)), graphTaskExecutor);
    }

    private GraphExecutionPayload execute(GraphPayload graphPayload, GraphExecutionPayload graphExecutionPayload) {
        Long graphExecutionId = graphExecutionPayload.getId();
        try {
            Graph graph = new Graph(graphPayload.getNodes());
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.RUNNING);

            Iterator<NodeExecution> iterator = graph.nodeIterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                NodeExecution node = iterator.next();
                nodeExecutor.launchExecution(node, graphExecutionId).join();

                processedNodes += 1;

                graphExecutionService.updateProgress(graphExecutionId, processedNodes);
                notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));
            }

            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.COMPLETED);
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            return graphExecutionService.findById(graphExecutionId);
        } catch (Exception e) {
            log.error("Error executing graph {}: {}", graphPayload.getId(), e.getMessage(), e);
            graphExecutionService.markFailed(graphExecutionId, e.getMessage());
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            throw new GraphExecutionException(e);
        }
    }
}
