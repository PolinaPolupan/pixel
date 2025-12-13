package com.example.pixel.node_execution.executor;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.dto.NodeExecutionDto;
import com.example.pixel.node_execution.model.Node;
import com.example.pixel.node_execution.dto.NodeClientData;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import com.example.pixel.node_execution.service.NodeExecutionService;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class AsyncNodeExecutor implements NodeExecutor {

    private static final String NODE_EXECUTION_FAILED_MESSAGE = "Node execution for node with id %s failed: %s";

    private final NodeExecutionService nodeExecutionService;

    public CompletableFuture<NodeExecutionDto> launchExecution(Node node, Long graphExecutionId) {
        return CompletableFuture.supplyAsync(() -> execute(node, graphExecutionId));
    }

    public NodeExecutionDto execute(Node node, Long graphExecutionId) {
        NodeExecutionEntity nodeExecutionEntity = nodeExecutionService.create(node, graphExecutionId);

        try {
            NodeClientData data = nodeExecutionService.setup(node, graphExecutionId);
            nodeExecutionService.validate(data);
            NodeExecutionResponse nodeExecutionResponse = nodeExecutionService.execute(data);

            nodeExecutionService.complete(nodeExecutionEntity.getId(), node, nodeExecutionResponse);

            return nodeExecutionService.findById(nodeExecutionEntity.getId());
        } catch (Exception e) {
            nodeExecutionService.failed(nodeExecutionEntity.getId(), node, e.getMessage());
            throw new NodeExecutionException(
                    String.format(NODE_EXECUTION_FAILED_MESSAGE, node.getId(), e.getMessage()), e
            );
        }
    }
}
