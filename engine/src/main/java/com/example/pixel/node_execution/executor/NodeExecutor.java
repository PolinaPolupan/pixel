package com.example.pixel.node_execution.executor;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.dto.Metadata;
import com.example.pixel.node_execution.dto.NodeClientData;
import com.example.pixel.node_execution.cache.NodeCache;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.dto.NodeValidationResponse;
import com.example.pixel.node_execution.integration.NodeClient;
import com.example.pixel.node.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class NodeExecutor {

    private final NodeClient nodeClient;
    private final NodeCache nodeCache;

    public NodeClientData setup(Node node, Long graphExecutionId) {
        Map<String, Object> resolvedInputs = resolveInputs(node, graphExecutionId);
        node.setInputs(resolvedInputs);

        Metadata meta = new Metadata(node.getType(), node.getId(), graphExecutionId);

        return new NodeClientData(meta, resolvedInputs);
    }

    public void execute(NodeClientData nodeClientData) {
        NodeExecutionResponse executionResponse = nodeClient.executeNode(nodeClientData);

        String outputKey = getOutputKey(nodeClientData.getMeta().getGraphExecutionId(), nodeClientData.getMeta().getNodeId());
        nodeCache.put(outputKey, executionResponse.getOutputs());

        log.info("Node {} Exec Output JSON | Response: {}", nodeClientData.getMeta().getNodeId(), executionResponse);
    }

    public void validate(NodeClientData nodeClientData) {
        NodeValidationResponse validationResponse = nodeClient.validateNode(nodeClientData);

        String inputKey = getInputKey(nodeClientData.getMeta().getGraphExecutionId(), nodeClientData.getMeta().getNodeId());
        nodeCache.put(inputKey, nodeClientData.getInputs());

        log.info("Node {} Validation Input JSON: {} | Response: {}", nodeClientData.getMeta().getNodeId(), nodeClientData, validationResponse);
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
                    "Missing cache for node " + reference.getNodeId() + " in task " + graphExecutionId
            );
        }

        Map<String, Object> outputMap = nodeCache.get(cacheKey);

        if (!outputMap.containsKey(output)) {
            throw new NodeExecutionException(
                    "Output '" + output + "' not found in node " + reference.getNodeId() + " for task " + graphExecutionId
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
