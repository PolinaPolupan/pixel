package com.example.pixel.node_execution.service;

import com.example.pixel.node.model.Node;
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

    public NodeExecutionEntity execute(Node node, Long graphExecutionId) {
        NodeClientData data = nodeExecutor.setup(node, graphExecutionId);
        nodeExecutor.validate(data);
        Instant startedAt = Instant.now();
        NodeExecutionResponse nodeExecutionResponse = nodeExecutor.execute(data);

        NodeExecutionEntity nodeExecutionEntity = NodeExecutionEntity
                .builder()
                .status(NodeStatus.PENDING)
                .inputs(node.getInputs())
                .outputs(nodeExecutionResponse.getOutputs())
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .build();

        return repository.save(nodeExecutionEntity);
    }
}
