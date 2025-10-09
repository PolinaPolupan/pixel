package com.example.pixel.node_execution.service;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.dto.*;
import com.example.pixel.node_execution.cache.NodeCache;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import com.example.pixel.node_execution.integration.NodeClient;
import com.example.pixel.node_execution.model.NodeExecution;
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

    @Transactional
    public NodeExecutionEntity create(NodeExecution nodeExecution, Long graphExecutionId) {
        Instant startedAt = Instant.now();
        NodeExecutionEntity nodeExecutionEntity = NodeExecutionEntity.builder()
                .status(NodeStatus.RUNNING)
                .graphExecutionId(graphExecutionId)
                .inputs(nodeExecution.getInputs())
                .startedAt(startedAt)
                .build();

        return repository.save(nodeExecutionEntity);
    }

    @Transactional(readOnly = true)
    public NodeExecutionEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NodeExecutionException(NODE_EXECUTION_NOT_FOUND_MESSAGE + id));
    }

    @Transactional
    public void complete(Long id, NodeExecution nodeExecution, NodeExecutionResponse nodeExecutionResponse) {
        NodeExecutionEntity nodeExecutionEntity = findById(id);

        nodeExecutionEntity.setInputs(nodeExecution.getInputs());
        nodeExecutionEntity.setStatus(NodeStatus.COMPLETED);
        nodeExecutionEntity.setOutputs(nodeExecutionResponse.getOutputs());
        nodeExecutionEntity.setFinishedAt(Instant.now());

        repository.save(nodeExecutionEntity);
    }

    @Transactional
    public void failed(Long id, NodeExecution nodeExecution, String message) {
        NodeExecutionEntity nodeExecutionEntity = findById(id);

        nodeExecutionEntity.setInputs(nodeExecution.getInputs());
        nodeExecutionEntity.setStatus(NodeStatus.FAILED);
        nodeExecutionEntity.setErrorMessage(message);
        nodeExecutionEntity.setFinishedAt(Instant.now());

        repository.save(nodeExecutionEntity);
    }

    public NodeClientData setup(NodeExecution nodeExecution, Long graphExecutionId) {
        Map<String, Object> resolvedInputs = resolveInputs(nodeExecution, graphExecutionId);
        nodeExecution.setInputs(resolvedInputs);

        Metadata meta = new Metadata(nodeExecution.getType(), nodeExecution.getId(), graphExecutionId);

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

    private Map<String, Object> resolveInputs(NodeExecution nodeExecution, Long graphExecutionId) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: nodeExecution.getInputs().keySet()) {
            resolvedInputs.put(key, resolveInput(nodeExecution, graphExecutionId, key));
        }

        return resolvedInputs;
    }

    private Object resolveInput(NodeExecution nodeExecution, Long graphExecutionId, String key) {
        Object input = nodeExecution.getInputs().get(key);

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
