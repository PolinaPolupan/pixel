package com.example.mypixel.node;

import com.example.mypixel.common.PerformanceTracker;
import io.micrometer.core.instrument.Tags;
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
    private final PerformanceTracker performanceTracker;

    public void processNode(Node node, Long sceneId, Long taskId) {

        Tags nodeTags = Tags.of(
                "node.id", String.valueOf(node.getId()),
                "node.type", node.getType(),
                "scene.id", String.valueOf(sceneId),
                "task.id", String.valueOf(taskId)
        );

        performanceTracker.trackOperation(
                "node.execution",
                nodeTags,
                () -> processNodeInternal(node, sceneId, taskId)
        );
    }

    @SuppressWarnings("unchecked")
    public void processNodeInternal(Node node, Long sceneId, Long taskId) {
        log.info("Started node: {} Scene: {} Task: {}", node.getId(), sceneId, taskId);

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> resolvedInputs = resolveInputs(node, taskId);
        Map<String, Object> meta = Map.of(
                "node_id", node.getId(),
                "type", node.getType(),
                "scene_id", sceneId,
                "task_id", taskId
        );
        data.put("meta", meta);
        data.put("inputs", resolvedInputs);
        node.setInputs(resolvedInputs);

        Map<String, Object> response = nodeCommunicationService.executeNodeRequest("validate", data, Map.class);
        log.info("Node {} Validation Input JSON: {} | Response: {}", node.getId(), data, response);

        String outputKey = taskId + ":" + node.getId() + ":output";
        String inputKey = taskId + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());

        Map<String, Object> outputs = nodeCommunicationService.executeNodeRequest("exec", data, Map.class);
        log.info("Node {} Exec Output JSON: {} | Response: {}", node.getId(), data, response);

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
