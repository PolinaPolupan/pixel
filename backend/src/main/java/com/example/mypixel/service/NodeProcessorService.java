package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class NodeProcessorService {

    private final AutowireCapableBeanFactory beanFactory;
    private final NodeCacheService nodeCacheService;
    private final StorageService storageService;
    private final BatchProcessor batchProcessor;

    public void processNode(Node node,
                            Long sceneId,
                            Long taskId,
                            Map<Long, Node> nodeMap) {
        beanFactory.autowireBean(node);
        FileHelper fileHelper = new FileHelper(storageService, node, sceneId, taskId);
        node.setFileHelper(fileHelper);
        node.setBatchProcessor(batchProcessor);

        log.info("Started node: {}", node.getId());

        resolveInputs(node, taskId, nodeMap);
        node.validate();

        String outputKey = taskId + ":" + node.getId() + ":output";
        String inputKey = taskId + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());
        nodeCacheService.put(outputKey, node.exec());
    }

    private Object resolveReference(NodeReference reference, Long taskId, Map<Long, Node> nodeMap) {
        Long id = reference.getNodeId();
        String output = reference.getOutputName();

        if (!nodeMap.containsKey(id)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id " +
                    id + " is not found. Please ensure the node id is correct.");
        }

        if (!nodeMap.get(id).getOutputTypes().containsKey(output)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id "
                    + id + " does not contain output '" + output
                    + "'. Available outputs are: " + nodeMap.get(id).getOutputTypes().keySet());
        }

        return nodeCacheService.get(taskId + ":" + id + ":output").get(output);
    }

    private Object castTypes(Node node, Object value, ParameterType requiredType) {
        if (value == null) {
            throw new InvalidNodeParameter("Cannot cast null to " + requiredType + " type");
        }
        return switch (requiredType) {
            case FLOAT -> value instanceof Number ? ((Number) value).floatValue() : (float) value;
            case INT -> value instanceof Number ? ((Number) value).intValue() : (int) value;
            case DOUBLE -> value instanceof Number ? ((Number) value).doubleValue() : (double) value;
            case STRING -> (String) value;
            case VECTOR2D -> Vector2D.fromMap((Map<String, Object>) value);
            case FILEPATH_ARRAY -> {
                HashSet<String> files = new HashSet<>();
                if (value instanceof Collection<?>) {
                    batchProcessor.processBatches(
                            (Collection<?>) value,
                            item -> {
                                if (item instanceof String file) {
                                    files.add(node.getFileHelper().createDump(file));
                                } else {
                                    throw new InvalidNodeParameter(
                                            "Invalid file path: expected String but got " +
                                                    (item != null ? item.getClass().getSimpleName() : "null")
                                    );
                                }
                            }
                    );
                }
                yield files;
            }
            case STRING_ARRAY -> (List<String>) value;
        };
    }

    private void resolveInputs(Node node,
                               Long taskId,
                               Map<Long, Node> nodeMap) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputTypes().keySet()) {
            // If the user's inputs don't contain one of the parameters
            if (!node.getInputs().containsKey(key)) {
                // If it is required - throw an exception
                if (node.getInputTypes().get(key).isRequired()) {
                    throw new InvalidNodeParameter("Required input " + key
                            + " is not provided for the node with id " + node.getId());
                } else { // Omit, continue on processing other inputs
                    continue;
                }
            }

            resolvedInputs.put(key, resolveInput(node, taskId, key, nodeMap));
        }

        node.setInputs(resolvedInputs);
    }

    private Object resolveInput(Node node,
                                Long taskId,
                                String key,
                                Map<Long, Node> nodeMap) {
        Object input = node.getInputs().get(key);
        ParameterType requiredType = node.getInputTypes().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, taskId, nodeMap);
        }

        // Cast to required type
        try {
            input = castTypes(node, input, requiredType);
        } catch (ClassCastException e) {
            throw new InvalidNodeParameter(
                    "Invalid input parameter '" + key + "' to the node with id " +
                            node.getId() + ": cannot cast " + input.getClass().getSimpleName() +
                            " to " + requiredType + " type"
            );
        }

        return input;
    }
}
