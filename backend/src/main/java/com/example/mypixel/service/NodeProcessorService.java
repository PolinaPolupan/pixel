package com.example.mypixel.service;

import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
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

        resolveInputs(node);
        node.validate();

        String outputKey = node.getTaskId() + ":" + node.getId() + ":output";
        String inputKey = node.getTaskId() + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());
        nodeCacheService.put(outputKey, node.exec());
    }

    private void resolveInputs(Node node) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputs().keySet()) {
            resolvedInputs.put(key, resolveInput(node, key));
        }

        node.setInputs(resolvedInputs);
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
