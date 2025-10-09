package com.example.pixel.node_execution.executor;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.model.NodeExecution;
import com.example.pixel.node_execution.dto.NodeClientData;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import com.example.pixel.node_execution.service.NodeExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class NodeExecutor {

    private static final String NODE_EXECUTION_FAILED_MESSAGE = "Node execution for node with id %s failed: %s";

    private final NodeExecutionService nodeExecutionService;

    public CompletableFuture<Void> executeAsync(NodeExecution nodeExecution, Long graphExecutionId) {
        return CompletableFuture.runAsync(() -> execute(nodeExecution, graphExecutionId));
    }

    public void execute(NodeExecution nodeExecution, Long graphExecutionId) {
        NodeExecutionEntity nodeExecutionEntity = nodeExecutionService.create(nodeExecution, graphExecutionId);

        try {
            NodeClientData data = nodeExecutionService.setup(nodeExecution, graphExecutionId);
            nodeExecutionService.validate(data);
            NodeExecutionResponse nodeExecutionResponse = nodeExecutionService.execute(data);

            nodeExecutionService.complete(nodeExecutionEntity.getId(), nodeExecution, nodeExecutionResponse);
        } catch (Exception e) {
            nodeExecutionService.failed(nodeExecutionEntity.getId(), nodeExecution, e.getMessage());

            throw new NodeExecutionException(
                    String.format(NODE_EXECUTION_FAILED_MESSAGE, nodeExecution.getId(), e.getMessage()), e
            );
        }
    }
}
