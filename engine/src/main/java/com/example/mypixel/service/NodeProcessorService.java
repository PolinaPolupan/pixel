package com.example.mypixel.service;

import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class NodeProcessorService {

    private final NodeCacheService nodeCacheService;
    private final PerformanceTracker performanceTracker;
    private final TypeConverterRegistry typeConverterRegistry;
    private final ObjectMapper objectMapper;


    public void processNode(Node node, Long sceneId, Long taskId) {
        node.setSceneId(sceneId);
        node.setTaskId(taskId);

        Tags nodeTags = Tags.of(
                "node.id", String.valueOf(node.getId()),
                "node.type", node.getType(),
                "scene.id", String.valueOf(node.getSceneId()),
                "task.id", String.valueOf(node.getTaskId())
        );

        performanceTracker.trackOperation(
                "node.execution",
                nodeTags,
                () -> processNodeInternal(node)
        );
    }

    public void processNodeInternal(Node node) {
        log.debug("Started node: {}", node.getId());

        Map<String, Object> resolvedInputs = resolveInputs(node);
        node.setInputs(resolvedInputs);

        try {
            String inputJson = objectMapper.writeValueAsString(resolvedInputs);
            log.info("Node {} Input JSON: {}", node.getId(), inputJson);
        } catch (Exception e) {
            log.warn("Failed to serialize input JSON for node {}: {}", node.getId(), e.getMessage());
        }

        node.validate();

        String outputKey = node.getTaskId() + ":" + node.getId() + ":output";
        String inputKey = node.getTaskId() + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());

        Map<String, Object> outputs = node.exec();

        try {
            String outputJson = objectMapper.writeValueAsString(outputs);
            log.info("Node {} Output JSON: {}", node.getId(), outputJson);
        } catch (Exception e) {
            log.warn("Failed to serialize output JSON for node {}: {}", node.getId(), e.getMessage());
        }

        nodeCacheService.put(outputKey, outputs);
    }


    private Map<String, Object> resolveInputs(Node node) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputs().keySet()) {
            resolvedInputs.put(key, resolveInput(node, key));
        }

        return resolvedInputs;
    }

    private Object resolveInput(Node node, String key) {
        Object input = node.getInputs().get(key);
        Parameter requiredType = node.getInputTypes().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, node.getTaskId());
        }
        // Cast to required type
        input = typeConverterRegistry.convert(input, requiredType, node) ;

        return input;
    }

    private Object resolveReference(NodeReference reference, Long taskId) {
        try {
            Long id = reference.getNodeId();
            String output = reference.getOutputName();

            String cacheKey = taskId + ":" + id + ":output";

            if (!nodeCacheService.exists(cacheKey)) {
                throw new RuntimeException("Failed to resolve reference: " + reference.getReference());
            }

            Map<String, Object> outputMap = nodeCacheService.get(cacheKey);

            if (!outputMap.containsKey(output)) {
                throw new RuntimeException("Failed to resolve reference: " + reference.getReference());
            }

            return outputMap.get(output);
        } catch (Exception e) {
            log.error("Error resolving reference: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve reference: " + e.getMessage(), e);
        }
    }
}
