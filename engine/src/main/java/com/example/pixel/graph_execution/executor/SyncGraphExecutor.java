package com.example.pixel.graph_execution.executor;

import com.example.pixel.common.exception.GraphExecutionException;
import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph.model.Graph;
import com.example.pixel.graph_execution.dto.GraphExecutionDto;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import com.example.pixel.node_execution.executor.NodeExecutor;
import com.example.pixel.node_execution.model.NodeExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class SyncGraphExecutor implements GraphExecutor {

    private final NodeExecutor nodeExecutor;
    private final GraphExecutionService graphExecutionService;
    private final NotificationService notificationService;

    public CompletableFuture<GraphExecutionDto> launchExecution(GraphDto graphDto, GraphExecutionDto graphExecutionDto) {
        log.info("startGraphExecution created with id={}, launching async graph execution ...", graphExecutionDto.getId());
        return CompletableFuture.completedFuture(execute(graphDto, graphExecutionDto));
    }

    private GraphExecutionDto execute(GraphDto graphDto, GraphExecutionDto graphExecutionDto) {
        Long graphExecutionId = graphExecutionDto.getId();
        try {
            Graph graph = new Graph(graphDto.getNodes());
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
            log.error("Error executing graph {}: {}", graphDto.getId(), e.getMessage(), e);
            graphExecutionService.markFailed(graphExecutionId, e.getMessage());
            notificationService.sendTaskStatus(graphExecutionService.findById(graphExecutionId));

            throw new GraphExecutionException(e);
        }
    }
}
