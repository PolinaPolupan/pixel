package com.example.pixel.graph_execution.executor;

import com.example.pixel.common.exception.GraphExecutionException;
import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph.model.Graph;
import com.example.pixel.node_execution.dto.NodeExecutionDto;
import com.example.pixel.node_execution.executor.NodeExecutor;
import com.example.pixel.node_execution.model.Node;
import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph_execution.dto.GraphExecutionDto;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Slf4j
@RequiredArgsConstructor
public class LevelGraphExecutor implements GraphExecutor {

    private final NodeExecutor nodeExecutor;
    private final GraphExecutionService graphExecutionService;
    private final NotificationService notificationService;
    private final Executor graphTaskExecutor;

    public CompletableFuture<GraphExecutionDto> launchExecution(GraphDto graphDto, GraphExecutionDto graphExecutionDto) {
        log.info("startGraphExecution created with id={}, launching async graph execution ...", graphExecutionDto.getId());
        return CompletableFuture.supplyAsync(() -> {
            execute(graphDto, graphExecutionDto);
            return graphExecutionDto;
        }, graphTaskExecutor);
    }

    private GraphExecutionDto execute(GraphDto graphDto, GraphExecutionDto graphExecutionDto) {
        Long graphExecutionId = graphExecutionDto.getId();
        try {
            Graph graph = new Graph(graphDto);
            graphExecutionService.updateStatus(graphExecutionId, GraphExecutionStatus.RUNNING);

            Iterator<List<Node>> iterator = graph.levelIterator();
            int processedNodes = 0;

            while (iterator.hasNext()) {
                List<Node> batch = iterator.next();
                List<CompletableFuture<NodeExecutionDto>> futures = batch.stream()
                        .map(node -> nodeExecutor.launchExecution(node, graphExecutionId))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                processedNodes += batch.size();
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