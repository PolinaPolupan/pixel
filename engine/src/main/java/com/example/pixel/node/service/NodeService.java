package com.example.pixel.node.service;

import com.example.pixel.node.dto.NodeConfiguration;
import com.example.pixel.node.dto.NodeDto;
import com.example.pixel.node.entity.NodeEntity;
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

    public NodeDto create(NodeConfiguration nodeConfiguration) {
        NodeEntity latest = repository.findLatestByType(nodeConfiguration.getType()).orElse(null);
        int nextVersion = 1;

        if (latest != null) {
            nextVersion = latest.getVersion() + 1;
            latest.setActive(false);
            repository.save(latest);
        }

        NodeEntity nodeEntity = NodeEntity.builder()
                .type(nodeConfiguration.getType())
                .inputs(nodeConfiguration.getInputs())
                .outputs(nodeConfiguration.getOutputs())
                .display(nodeConfiguration.getDisplay())
                .createdAt(Instant.now())
                .version(nextVersion)
                .active(true)
                .build();

        nodeEntity = repository.save(nodeEntity);

        return nodeMapper.toDto(nodeEntity);
    }

    public Map<String, NodeDto> getAllActiveNodes() {
        List<NodeEntity> activeNodes = repository.findByActiveTrue();

        Map<String, NodeDto> result = new HashMap<>();
        for (NodeEntity node: activeNodes) {
            result.put(node.getType(), nodeMapper.toDto(node));
        }
        return result;
    }
}
