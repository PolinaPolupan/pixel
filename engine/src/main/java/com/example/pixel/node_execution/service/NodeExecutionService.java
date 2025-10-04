package com.example.pixel.node_execution.service;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.model.NodeExecution;
import com.example.pixel.node_execution.dto.NodeClientData;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.dto.NodeStatus;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import com.example.pixel.node_execution.executor.NodeExecutor;
import com.example.pixel.node_execution.repository.NodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class NodeExecutionService {

    private static final String NODE_EXECUTION_FAILED_MESSAGE = "Node execution for node with id %s failed: %s";

    private final NodeExecutionRepository repository;
    private final NodeExecutor nodeExecutor;

    public CompletableFuture<Void> executeAsync(NodeExecution nodeExecution, Long graphExecutionId) {
        return CompletableFuture.runAsync(() -> execute(nodeExecution, graphExecutionId));
    }

    public void execute(NodeExecution nodeExecution, Long graphExecutionId) {
        Instant startedAt = Instant.now();
        NodeExecutionEntity nodeExecutionEntity = NodeExecutionEntity.builder()
                .status(NodeStatus.RUNNING)
                .graphExecutionId(graphExecutionId)
                .inputs(nodeExecution.getInputs())
                .startedAt(startedAt)
                .build();

        nodeExecutionEntity = repository.save(nodeExecutionEntity);

        try {
            NodeClientData data = nodeExecutor.setup(nodeExecution, graphExecutionId);
            nodeExecutor.validate(data);
            NodeExecutionResponse nodeExecutionResponse = nodeExecutor.execute(data);

            nodeExecutionEntity.setInputs(nodeExecution.getInputs());
            nodeExecutionEntity.setStatus(NodeStatus.COMPLETED);
            nodeExecutionEntity.setOutputs(nodeExecutionResponse.getOutputs());
        } catch (Exception e) {
            nodeExecutionEntity.setInputs(nodeExecution.getInputs());
            nodeExecutionEntity.setStatus(NodeStatus.FAILED);
            nodeExecutionEntity.setErrorMessage(e.getMessage());

            throw new NodeExecutionException(
                    String.format(NODE_EXECUTION_FAILED_MESSAGE, nodeExecution.getId(), e.getMessage()), e
            );
        } finally {
            nodeExecutionEntity.setFinishedAt(Instant.now());
            repository.save(nodeExecutionEntity);
        }
    }
}
