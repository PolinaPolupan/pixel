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

    @SuppressWarnings("unchecked")
    public void processNode(Node node, Long sceneId, Long taskId) {
        log.info("Started node: {} Scene: {} Task: {}", node.getId(), sceneId, taskId);

        Map<String, Object> resolvedInputs = resolveInputs(node, taskId);
        Metadata meta = new Metadata(node.getId(), sceneId, taskId, node.getType());
        NodeData nodeData = new NodeData(meta, resolvedInputs);
        node.setInputs(resolvedInputs);

        Map<String, Object> response = nodeCommunicationService.executeNodeRequest("/validate", nodeData, Map.class);
        log.info("Node {} Validation Input JSON: {} | Response: {}", node.getId(), nodeData, response);

        String outputKey = taskId + ":" + node.getId() + ":output";
        String inputKey = taskId + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());

        Map<String, Object> outputs = nodeCommunicationService.executeNodeRequest("/exec", nodeData, Map.class);
        log.info("Node {} Exec Output JSON: {} | Response: {}", node.getId(), nodeData, response);

        nodeCacheService.put(outputKey, outputs);
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
        Long id = reference.getNodeId();
        String output = reference.getOutputName();

        String cacheKey = taskId + ":" + id + ":output";

        if (!nodeCacheService.exists(cacheKey)) {
            throw new NodeExecutionException("Failed to resolve reference: " + reference.getReference());
        }

        Map<String, Object> outputMap = nodeCacheService.get(cacheKey);

        if (!outputMap.containsKey(output)) {
            throw new NodeExecutionException("Failed to resolve reference: " + reference.getReference());
        }

        return outputMap.get(output);
    }
}
