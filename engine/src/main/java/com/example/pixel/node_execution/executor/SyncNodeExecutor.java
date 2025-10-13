package com.example.pixel.node_execution.executor;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.dto.NodeClientData;
import com.example.pixel.node_execution.dto.NodeExecutionPayload;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import com.example.pixel.node_execution.model.NodeExecution;
import com.example.pixel.node_execution.service.NodeExecutionService;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class SyncNodeExecutor implements NodeExecutor {

    private static final String NODE_EXECUTION_FAILED_MESSAGE = "Node execution for node with id %s failed: %s";

    private final NodeExecutionService nodeExecutionService;

    public CompletableFuture<NodeExecutionPayload> launchExecution(NodeExecution nodeExecution, Long graphExecutionId) {
        return CompletableFuture.completedFuture(execute(nodeExecution, graphExecutionId));
    }

    public NodeExecutionPayload execute(NodeExecution nodeExecution, Long graphExecutionId) {
        NodeExecutionEntity nodeExecutionEntity = nodeExecutionService.create(nodeExecution, graphExecutionId);

        try {
            NodeClientData data = nodeExecutionService.setup(nodeExecution, graphExecutionId);
            nodeExecutionService.validate(data);
            NodeExecutionResponse nodeExecutionResponse = nodeExecutionService.execute(data);

            nodeExecutionService.complete(nodeExecutionEntity.getId(), nodeExecution, nodeExecutionResponse);

            return nodeExecutionService.findById(nodeExecutionEntity.getId());
        } catch (Exception e) {
            nodeExecutionService.failed(nodeExecutionEntity.getId(), nodeExecution, e.getMessage());
            throw new NodeExecutionException(
                    String.format(NODE_EXECUTION_FAILED_MESSAGE, nodeExecution.getId(), e.getMessage()), e
            );
        }
    }
}
