package com.example.pixel.node_execution.service;

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

import static org.reflections.Reflections.log;

@RequiredArgsConstructor
@Service
public class NodeExecutionService {

    private final NodeExecutionRepository repository;
    private final NodeExecutor nodeExecutor;

    public NodeExecutionEntity execute(NodeExecution nodeExecution, Long graphExecutionId) {
        Instant startedAt = Instant.now();
        NodeExecutionEntity nodeExecutionEntity = NodeExecutionEntity
                .builder()
                .status(NodeStatus.RUNNING)
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
            repository.save(nodeExecutionEntity);
        } catch (Exception e) {
            nodeExecutionEntity.setInputs(nodeExecution.getInputs());
            nodeExecutionEntity.setStatus(NodeStatus.FAILED);
            nodeExecutionEntity.setErrorMessage(e.getMessage());
            log.error("Node execution failed", e);
            throw e;
        } finally {
            nodeExecutionEntity.setFinishedAt(Instant.now());
        }

        return repository.save(nodeExecutionEntity);
    }
}
