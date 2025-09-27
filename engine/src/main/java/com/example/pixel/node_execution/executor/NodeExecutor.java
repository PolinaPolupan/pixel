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

    public void execute(Node node, Long graphId, Long taskId) {
        log.info("Started node: {} Graph: {} Task: {}", node.getId(), graphId, taskId);

        Map<String, Object> resolvedInputs = resolveInputs(node, taskId);
        Metadata meta = new Metadata(node.getId(), graphId, taskId, node.getType());
        NodeClientData nodeClientData = new NodeClientData(meta, resolvedInputs);
        node.setInputs(resolvedInputs);

        NodeValidationResponse validationResponse = nodeClient.validateNode(nodeClientData);
        log.info("Node {} Validation Input JSON: {} | Response: {}", node.getId(), nodeClientData, validationResponse);

        String inputKey = getInputKey(taskId, node.getId());
        nodeCache.put(inputKey, node.getInputs());

        String outputKey = getOutputKey(taskId, node.getId());
        NodeExecutionResponse executionResponse = nodeClient.executeNode(nodeClientData);
        log.info("Node {} Exec Output JSON | Response: {}", node.getId(), executionResponse);

        nodeCache.put(outputKey, executionResponse.getOutputs());
    }


    private Map<String, Object> resolveInputs(Node node, Long taskId) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputs().keySet()) {
            resolvedInputs.put(key, resolveInput(node, taskId, key));
        }

        return resolvedInputs;
    }

    private Object resolveInput(Node node, Long taskId, String key) {
        Object input = node.getInputs().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, taskId);
        }

        return input;
    }

    private Object resolveReference(NodeReference reference, Long taskId) {
        String output = reference.getOutputName();
        String cacheKey = getOutputKey(taskId, reference.getNodeId());

        if (!nodeCache.exists(cacheKey)) {
            throw new NodeExecutionException(
                    "Missing cache for node " + reference.getNodeId() + " in task " + taskId
            );
        }

        Map<String, Object> outputMap = nodeCache.get(cacheKey);

        if (!outputMap.containsKey(output)) {
            throw new NodeExecutionException(
                    "Output '" + output + "' not found in node " + reference.getNodeId() + " for task " + taskId
            );
        }

        return outputMap.get(output);
    }

    private String getOutputKey(Long taskId, Long nodeId) {
        return taskId + ":" + nodeId + ":output";
    }

    private String getInputKey(Long taskId, Long nodeId) {
        return taskId + ":" + nodeId + ":input";
    }
}
