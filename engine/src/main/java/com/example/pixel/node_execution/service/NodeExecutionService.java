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

@RequiredArgsConstructor
@Service
public class NodeExecutionService {

    private final NodeExecutionRepository repository;
    private final NodeExecutor nodeExecutor;

    public NodeExecutionEntity execute(NodeExecution nodeExecution, Long graphExecutionId) {
        NodeClientData data = nodeExecutor.setup(nodeExecution, graphExecutionId);
        nodeExecutor.validate(data);
        Instant startedAt = Instant.now();
        NodeExecutionResponse nodeExecutionResponse = nodeExecutor.execute(data);

        NodeExecutionEntity nodeExecutionEntity = NodeExecutionEntity
                .builder()
                .status(NodeStatus.PENDING)
                .inputs(nodeExecution.getInputs())
                .outputs(nodeExecutionResponse.getOutputs())
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .build();

        return repository.save(nodeExecutionEntity);
    }
}
