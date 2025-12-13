package com.example.pixel.node_execution.service;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.dto.*;
import com.example.pixel.node_execution.cache.NodeCache;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import com.example.pixel.common.integration.NodeClient;
import com.example.pixel.node_execution.mapper.NodeExecutionMapper;
import com.example.pixel.node_execution.model.Node;
import com.example.pixel.node_execution.model.NodeReference;
import com.example.pixel.node_execution.repository.NodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class NodeExecutionService {
    private final static String NODE_EXECUTION_NOT_FOUND_MESSAGE = "Node execution not found: ";
    private final static String UNABLE_TO_FIND_NODE_IN_CACHE = "Unable to find node with id %s in cache. Graph execution id: %s";
    private final static String UNABLE_TO_FIND_OUTPUT_IN_CACHE = "Unable to find output '%s' for node with id %s in cache. Graph execution id: %s";

    private final NodeClient nodeClient;
    private final NodeCache nodeCache;
    private final NodeExecutionRepository repository;
    private final NodeExecutionMapper mapper;

    @Transactional
    public NodeExecutionEntity create(Node node, Long graphExecutionId) {
        Instant startedAt = Instant.now();
        NodeExecutionEntity nodeExecutionEntity = NodeExecutionEntity.builder()
                .status(NodeStatus.RUNNING)
                .graphExecutionId(graphExecutionId)
                .inputs(node.getInputs())
                .startedAt(startedAt)
                .build();

        return repository.save(nodeExecutionEntity);
    }

    @Transactional(readOnly = true)
    public NodeExecutionDto findById(Long id) {
        NodeExecutionEntity executionEntity = repository.findById(id)
                .orElseThrow(() -> new NodeExecutionException(NODE_EXECUTION_NOT_FOUND_MESSAGE + id));
        return mapper.toDto(executionEntity);
    }

    @Transactional
    public void complete(Long id, Node node, NodeExecutionResponse nodeExecutionResponse) {
        NodeExecutionEntity nodeExecutionEntity = repository.findById(id)
                .orElseThrow(() -> new NodeExecutionException(NODE_EXECUTION_NOT_FOUND_MESSAGE + id));

        nodeExecutionEntity.setInputs(node.getInputs());
        nodeExecutionEntity.setStatus(NodeStatus.COMPLETED);
        nodeExecutionEntity.setOutputs(nodeExecutionResponse.getOutputs());
        nodeExecutionEntity.setFinishedAt(Instant.now());

        repository.save(nodeExecutionEntity);
    }

    @Transactional
    public void failed(Long id, Node node, String message) {
        NodeExecutionEntity nodeExecutionEntity = repository.findById(id)
                .orElseThrow(() -> new NodeExecutionException(NODE_EXECUTION_NOT_FOUND_MESSAGE + id));

        nodeExecutionEntity.setInputs(node.getInputs());
        nodeExecutionEntity.setStatus(NodeStatus.FAILED);
        nodeExecutionEntity.setErrorMessage(message);
        nodeExecutionEntity.setFinishedAt(Instant.now());

        repository.save(nodeExecutionEntity);
    }

    public NodeClientData setup(Node node, Long graphExecutionId) {
        Map<String, Object> resolvedInputs = resolveInputs(node, graphExecutionId);
        node.setInputs(resolvedInputs);

        Metadata meta = new Metadata(node.getType(), node.getId(), graphExecutionId);

        return new NodeClientData(meta, resolvedInputs);
    }

    public NodeExecutionResponse execute(NodeClientData nodeClientData) {
        NodeExecutionResponse executionResponse = nodeClient.execute(nodeClientData);

        String outputKey = getOutputKey(nodeClientData.getMeta().getGraphExecutionId(), nodeClientData.getMeta().getNodeId());
        nodeCache.put(outputKey, executionResponse.getOutputs());

        log.info("Node {} Exec | Response: {}", nodeClientData.getMeta().getNodeId(), executionResponse);
        return executionResponse;
    }

    public void validate(NodeClientData nodeClientData) {
        NodeValidationResponse validationResponse = nodeClient.validate(nodeClientData);

        String inputKey = getInputKey(nodeClientData.getMeta().getGraphExecutionId(), nodeClientData.getMeta().getNodeId());
        nodeCache.put(inputKey, nodeClientData.getInputs());

        log.info("Node {} Validation | Response: {}", nodeClientData.getMeta().getNodeId(), validationResponse);
    }

    private Map<String, Object> resolveInputs(Node node, Long graphExecutionId) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputs().keySet()) {
            resolvedInputs.put(key, resolveInput(node, graphExecutionId, key));
        }

        return resolvedInputs;
    }

    private Object resolveInput(Node node, Long graphExecutionId, String key) {
        Object input = node.getInputs().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, graphExecutionId);
        }

        return input;
    }

    private Object resolveReference(NodeReference reference, Long graphExecutionId) {
        String output = reference.getOutputName();
        String cacheKey = getOutputKey(graphExecutionId, reference.getNodeId());

        if (!nodeCache.exists(cacheKey)) {
            throw new NodeExecutionException(
                    String.format(UNABLE_TO_FIND_NODE_IN_CACHE, reference.getNodeId(), graphExecutionId)
            );
        }

        Map<String, Object> outputMap = nodeCache.get(cacheKey);

        if (!outputMap.containsKey(output)) {
            throw new NodeExecutionException(
                    String.format(UNABLE_TO_FIND_OUTPUT_IN_CACHE, output, reference.getNodeId(), graphExecutionId)
            );
        }

        return outputMap.get(output);
    }

    private String getOutputKey(Long graphExecutionId, Long nodeId) {
        return graphExecutionId + ":" + nodeId + ":output";
    }

    private String getInputKey(Long graphExecutionId, Long nodeId) {
        return graphExecutionId + ":" + nodeId + ":input";
    }
}
