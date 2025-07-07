package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import io.micrometer.core.instrument.Tags;
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
    private final PerformanceTracker performanceTracker;

    public void processNode(
            Node node,
            Long sceneId,
            Long taskId
    ) {
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

    public void processNodeInternal(
            Node node,
            Long sceneId,
            Long taskId
    ) {
        beanFactory.autowireBean(node);
        FileHelper fileHelper = new FileHelper(storageService, node, sceneId, taskId);
        node.setFileHelper(fileHelper);
        node.setBatchProcessor(batchProcessor);

        log.debug("Started node: {}", node.getId());

        resolveInputs(node, taskId);
        node.validate();

        String outputKey = taskId + ":" + node.getId() + ":output";
        String inputKey = taskId + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());
        nodeCacheService.put(outputKey, node.exec());
    }

    private void resolveInputs(Node node, Long taskId) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputs().keySet()) {
            resolvedInputs.put(key, resolveInput(node, taskId, key));
        }

        node.setInputs(resolvedInputs);
    }

    private Object resolveInput(Node node,
                                Long taskId,
                                String key) {
        Object input = node.getInputs().get(key);
        Parameter requiredType = node.getInputTypes().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, taskId);
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

    private Object resolveReference(NodeReference reference, Long taskId) {
        try {
            Long id = reference.getNodeId();
            String output = reference.getOutputName();

            String cacheKey = taskId + ":" + id + ":output";

            if (!nodeCacheService.exists(cacheKey)) {
                throw new RuntimeException("Failed to resolve reference: " + reference.getReference());
            }

            Map<String, Object> outputMap = nodeCacheService.get(cacheKey);

            return outputMap.get(output);
        } catch (Exception e) {
            log.error("Error resolving reference: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve reference: " + e.getMessage(), e);
        }
    }

    private Object castTypes(Node node, Object value, Parameter requiredType) {
        if (value == null) {
            throw new InvalidNodeParameter("Cannot cast null to " + requiredType + " type");
        }
        return switch (requiredType.getType()) {
            case FLOAT -> ((Number) value).floatValue();
            case INT -> ((Number) value).intValue();
            case DOUBLE -> ((Number) value).doubleValue();
            case STRING -> (String) value;
            case VECTOR2D -> {
                if (value instanceof Vector2D) {
                    yield value;
                } else if (value instanceof Map) {
                    yield Vector2D.fromMap((Map<String, Object>) value);
                } else {
                    throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to Vector2D");
                }
            }
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
                } else {
                    throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName()
                            + " to FILEPATH_ARRAY");
                }
                yield files;
            }
            case STRING_ARRAY -> (List<String>) value;
        };
    }
}
