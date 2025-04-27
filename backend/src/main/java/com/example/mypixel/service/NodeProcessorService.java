package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class NodeProcessorService {

    private final AutowireCapableBeanFactory beanFactory;
    // Hash map to store nodes and their corresponding outputs
    private final Map<Long, Map<Long, Map<String, Object>>> nodeOutputs = new HashMap<>();
    private final StorageService storageService;

    @Autowired
    public NodeProcessorService(
            AutowireCapableBeanFactory beanFactory, StorageService storageService
    ) {
        this.beanFactory = beanFactory;
        this.storageService = storageService;
    }

    public void processNode(Node node, int batchSize, Map<Long, Node> nodeMap) {
        beanFactory.autowireBean(node);
        FileHelper fileHelper = new FileHelper(storageService, node);
        node.setFileHelper(fileHelper);

        Long sceneId = node.getSceneId();
        log.info("Started node: {}", node.getId());

        nodeOutputs.computeIfAbsent(sceneId, k -> new HashMap<>());
        resolveInputs(node, nodeMap, false);
        node.validate();

        if (node.getInputs().containsKey("files")) {
            List<String> outputFiles = new ArrayList<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            partitionFileInput(node, batchSize)
                    .forEachRemaining(batch ->
                            futures.add(CompletableFuture.runAsync(() -> {
                                BatchNodeWrapper wrapper = new BatchNodeWrapper(node, node.getInputs());
                                log.debug("Processing batch with size: {}", batch.size());
                                wrapper.getInputs().put("files", batch);

                                resolveInputs(node, nodeMap, true);
                                Map<String, Object> outputs = wrapper.exec();

                                Map<String, Object> mutableOutputs = new HashMap<>(outputs);
                                nodeOutputs.get(sceneId).put(wrapper.getId(), mutableOutputs);

                                if (wrapper.getOutputTypes().containsKey("files")) {
                                    outputFiles.addAll((List<String>) outputs.get("files"));
                                }
                            }))
                    );

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join();

            if (node.getOutputTypes().containsKey("files")) {
                nodeOutputs.get(sceneId).get(node.getId()).put("files", outputFiles);
            }
        } else {
            resolveInputs(node, nodeMap, true);
            nodeOutputs.get(sceneId).put(node.getId(), node.exec());
        }
    }

    private Object resolveReference(NodeReference reference, Long sceneId, Map<Long, Node> nodeMap) {
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

        return nodeOutputs.get(sceneId).get(id).get(output);
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
                List<String> files = new ArrayList<>();
                for (String file: (List<String>) value) {
                    if (createDump) {
                        files.add(node.getFileHelper().createDump(file));
                    } else {
                        files.add(file);
                    }
                }
                yield files;
            }
            case STRING_ARRAY -> (List<String>) value;
        };
    }

    private void resolveInputs(Node node, Map<Long, Node> nodeMap, boolean createDump) {
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

            resolvedInputs.put(key, resolveInput(node, key, nodeMap, createDump));
        }

        node.setInputs(resolvedInputs);
    }

    private Object resolveInput(Node node, String key, Map<Long, Node> nodeMap, boolean createDump) {
        Object input = node.getInputs().get(key);
        ParameterType requiredType = node.getInputTypes().get(key);

        if (input instanceof NodeReference) {
            input = resolveReference((NodeReference) input, node.getSceneId(), nodeMap);
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
        List<String> files = (List<String>) node.getInputs().get("files");
        return Iterators.partition(files.iterator(), batchSize);
    }
}
