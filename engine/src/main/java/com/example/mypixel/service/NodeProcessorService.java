package com.example.mypixel.service;

import com.example.mypixel.model.*;
import com.example.mypixel.model.Node;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class NodeProcessorService {

    private final NodeCacheService nodeCacheService;
    private final PerformanceTracker performanceTracker;
    private final ObjectMapper objectMapper;


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

        try {
            String inputJson = objectMapper.writeValueAsString(data);
            RestTemplate restTemplate = new RestTemplate();
            PythonNodeTester tester = new PythonNodeTester(restTemplate, "http://node:8000/validate");
            String response = tester.sendJsonToPython(inputJson);
            log.info("Node {} Validation Input JSON: {} | Response: {}", node.getId(), inputJson, response);
        } catch (Exception e) {
            log.warn("Failed to serialize input JSON for node {}: {}", node.getId(), e.getMessage());
        }

        String outputKey = taskId + ":" + node.getId() + ":output";
        String inputKey = taskId + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());

        Map<String, Object> outputs = Map.of();

        try {
            String outputJson = objectMapper.writeValueAsString(data);

            RestTemplate restTemplate = new RestTemplate();
            PythonNodeTester tester = new PythonNodeTester(restTemplate, "http://node:8000/exec");
            String response = tester.sendJsonToPython(outputJson);

            log.info("Node {} Exec Output JSON: {} | Response: {}", node.getId(), outputJson, response);

            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> responseMap = objectMapper.readValue(response, typeRef);
            outputs = responseMap;

            log.info("Deserialized response: {}", responseMap);

        } catch (Exception e) {
            log.warn("Failed to process JSON for node {}: {}", node.getId(), e.getMessage());
        }

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
