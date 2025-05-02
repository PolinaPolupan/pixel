package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Component
@Slf4j
public class NodeProcessorService {

    private final AutowireCapableBeanFactory beanFactory;
    private final NodeCacheService nodeCacheService;
    private final FileService fileService;
    private final Executor graphTaskExecutor;

    public void processNode(Node node,
                            Long sceneId,
                            Long taskId,
                            int batchSize,
                            Map<Long, Node> nodeMap) {
        beanFactory.autowireBean(node);
        FileHelper fileHelper = new FileHelper(fileService, node, sceneId, taskId);
        node.setFileHelper(fileHelper);

        log.info("Started node: {}", node.getId());

        resolveInputs(node, taskId, nodeMap, false);
        node.validate();

        String outputKey = taskId + ":" + node.getId() + ":output";
        String inputKey = taskId + ":" + node.getId() + ":input";

        nodeCacheService.put(inputKey, node.getInputs());

        if (node.getInputs().containsKey("files")) {
            HashSet<String> outputFiles = new HashSet<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            partitionFileInput(node, batchSize)
                    .forEachRemaining(batch ->
                            futures.add(CompletableFuture.runAsync(() -> {
                                BatchNodeWrapper wrapper = new BatchNodeWrapper(node, node.getInputs());
                                log.debug("Processing batch with size: {}", batch.size());
                                wrapper.getInputs().put("files", batch);

                                resolveInputs(node, sceneId, nodeMap, true);

                                Map<String, Object> outputs = wrapper.exec();
                                Map<String, Object> mutableOutputs = new HashMap<>(outputs);
                                nodeCacheService.put(outputKey, mutableOutputs);

                                if (wrapper.getOutputTypes().containsKey("files")) {
                                    outputFiles.addAll((HashSet<String>) outputs.get("files"));
                                }
                            }, graphTaskExecutor))
                    );

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join();

            if (node.getOutputTypes().containsKey("files")) {
                Map<String, Object> outputs = nodeCacheService.get(outputKey);
                outputs.put("files", outputFiles);
                nodeCacheService.put(outputKey, outputs);
            }
        } else {
            resolveInputs(node, sceneId, nodeMap, true);
            nodeCacheService.put(outputKey, node.exec());
        }
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

    private Object castTypes(Node node, Object value, ParameterType requiredType, boolean createDump) {
        if (value == null) {
            throw new InvalidNodeParameter("Cannot cast null to " + requiredType + " type");
        }
        return switch (requiredType) {
            case FLOAT -> value instanceof Number ? ((Number) value).floatValue() : (float) value;
            case INT -> value instanceof Number ? ((Number) value).intValue() : (int) value;
            case DOUBLE -> value instanceof Number ? ((Number) value).doubleValue() : (double) value;
            case STRING -> (String) value;
            case FILEPATH_ARRAY -> {
                HashSet<String> files = new HashSet<>();
                // Accept any Collection<String>, not just HashSet
                if (value instanceof Collection<?>) {
                    for (Object item : (Collection<?>) value) {
                        if (item instanceof String file) {
                            if (createDump) {
                                files.add(node.getFileHelper().createDump(file));
                            } else {
                                files.add(file);
                            }
                        } else {
                            throw new InvalidNodeParameter(
                                    "Invalid file path: expected String but got " +
                                            (item != null ? item.getClass().getSimpleName() : "null")
                            );
                        }
                    }
                }
                yield files;
            }
            case STRING_ARRAY -> (List<String>) value;
        };
    }

    private void resolveInputs(Node node,
                               Long taskId,
                               Map<Long, Node> nodeMap,
                               boolean createDump) {
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

            resolvedInputs.put(key, resolveInput(node, taskId, key, nodeMap, createDump));
        }

        node.setInputs(resolvedInputs);
    }

    private Object resolveInput(Node node,
                                Long taskId,
                                String key,
                                Map<Long, Node> nodeMap,
                                boolean createDump) {
        Object input = node.getInputs().get(key);
        ParameterType requiredType = node.getInputTypes().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, taskId, nodeMap);
        }

        // Cast to required type
        try {
            input = castTypes(node, input, requiredType, createDump);
        } catch (ClassCastException e) {
            throw new InvalidNodeParameter(
                    "Invalid input parameter '" + key + "' to the node with id " +
                            node.getId() + ": cannot cast " + input.getClass().getSimpleName() +
                            " to " + requiredType + " type"
            );
        }

        return input;
    }

    public UnmodifiableIterator<List<String>> partitionFileInput(Node node, int batchSize) {
        HashSet<String> files = (HashSet<String>) node.getInputs().get("files");
        return Iterators.partition(files.iterator(), batchSize);
    }
}
