package com.example.pixel.node;

import com.example.pixel.exception.NodeExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class NodeProcessorService {

    private final NodeCommunicationService nodeCommunicationService;
    private final NodeCacheService nodeCacheService;

    public void processNode(Node node, Long sceneId, Long taskId) {
        log.info("Started node: {} Scene: {} Task: {}", node.getId(), sceneId, taskId);

        Map<String, Object> resolvedInputs = resolveInputs(node, taskId);
        Metadata meta = new Metadata(node.getId(), sceneId, taskId, node.getType());
        NodeData nodeData = new NodeData(meta, resolvedInputs);
        node.setInputs(resolvedInputs);

        NodeValidationResponse validationResponse = nodeCommunicationService.validateNode(nodeData);
        log.info("Node {} Validation Input JSON: {} | Response: {}", node.getId(), nodeData, validationResponse);

        String inputKey = getInputKey(taskId, node.getId());
        nodeCacheService.put(inputKey, node.getInputs());

        String outputKey = getOutputKey(taskId, node.getId());
        NodeExecutionResponse executionResponse = nodeCommunicationService.executeNode(nodeData);
        log.info("Node {} Exec Output JSON | Response: {}", node.getId(), executionResponse);

        nodeCacheService.put(outputKey, executionResponse.getOutputs());
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

        if (!nodeCacheService.exists(cacheKey)) {
            throw new NodeExecutionException(
                    "Missing cache for node " + reference.getNodeId() + " in task " + taskId
            );
        }

        Map<String, Object> outputMap = nodeCacheService.get(cacheKey);

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
