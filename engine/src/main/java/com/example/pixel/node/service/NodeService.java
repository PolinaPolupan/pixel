package com.example.pixel.node.service;

import com.example.pixel.node.dto.NodeConfigurationRequest;
import com.example.pixel.node.dto.NodeConfigurationDto;
import com.example.pixel.node.entity.NodeConfigurationEntity;
import com.example.pixel.node.mapper.NodeMapper;
import com.example.pixel.node.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class NodeService {

    private final NodeMapper nodeMapper;
    private final NodeRepository repository;

    public NodeConfigurationDto create(NodeConfigurationRequest nodeConfigurationRequest) {
        NodeConfigurationEntity latest = repository.findLatestByType(nodeConfigurationRequest.getType()).orElse(null);
        int nextVersion = 1;

        if (latest != null) {
            nextVersion = latest.getVersion() + 1;
            latest.setActive(false);
            repository.save(latest);
        }

        NodeConfigurationEntity nodeConfigurationEntity = NodeConfigurationEntity.builder()
                .type(nodeConfigurationRequest.getType())
                .inputs(nodeConfigurationRequest.getInputs())
                .outputs(nodeConfigurationRequest.getOutputs())
                .display(nodeConfigurationRequest.getDisplay())
                .createdAt(Instant.now())
                .version(nextVersion)
                .active(true)
                .build();

        nodeConfigurationEntity = repository.save(nodeConfigurationEntity);

        return nodeMapper.toDto(nodeConfigurationEntity);
    }

    public Map<String, NodeConfigurationDto> getAllActiveNodes() {
        List<NodeConfigurationEntity> activeNodes = repository.findByActiveTrue();

        Map<String, NodeConfigurationDto> result = new HashMap<>();
        for (NodeConfigurationEntity node: activeNodes) {
            result.put(node.getType(), nodeMapper.toDto(node));
        }
        return result;
    }
}
