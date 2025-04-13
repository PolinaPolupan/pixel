package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class NodeProcessorService {

    private final AutowireCapableBeanFactory beanFactory;
    // Hash map to store nodes and their corresponding outputs
    private final Map<String, Map<Long, Map<String, Object>>> nodeOutputs = new HashMap<>();
    private final Map<String, Map<Long, Node>> nodeMap = new HashMap<>();
    private final FileManager fileManager;

    @Autowired
    public NodeProcessorService(
            AutowireCapableBeanFactory beanFactory,
            FileManager fileManager
    ) {
        this.beanFactory = beanFactory;
        this.fileManager = fileManager;
    }

    public void processNode(Node node) {
        beanFactory.autowireBean(node);

        String uuid = (String) node.getInputs().get("sceneId");

        nodeOutputs.computeIfAbsent(uuid, k -> new HashMap<>());
        nodeMap.computeIfAbsent(uuid, k -> new HashMap<>());

        nodeMap.get(uuid).put(node.getId(), node);
        resolveInputs(node);
        node.validate();
        nodeOutputs.get(uuid).put(node.getId(), node.exec());
        log.info(nodeOutputs.toString());
    }

    private Object resolveReference(NodeReference reference, String sceneId) {
        Long id = reference.getNodeId();
        String output = reference.getOutputName();

        if (!nodeMap.get(sceneId).containsKey(id)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id " +
                    id + " is not found. Please ensure the node id is correct.");
        }

        if (!nodeMap.get(sceneId).get(id).getOutputTypes().containsKey(output)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id "
                    + id + " does not contain output '" + output
                    + "'. Available outputs are: " + nodeMap.get(sceneId).get(id).getOutputTypes().keySet());
        }

        return nodeOutputs.get(sceneId).get(id).get(output);
    }

    private Object castTypes(String sceneId, Object value, ParameterType requiredType) {
        if (value == null) {
            throw new InvalidNodeParameter("Cannot cast null to " + requiredType + " type");
        }
        return switch (requiredType) {
            case FLOAT -> value instanceof Number ? ((Number) value).floatValue() : (float) value;
            case INT -> value instanceof Number ? ((Number) value).intValue() : (int) value;
            case DOUBLE -> value instanceof Number ? ((Number) value).doubleValue() : (double) value;
            case STRING -> (String) value;
            case FILENAMES_ARRAY -> {
                List<String> files = new ArrayList<>();
                for (String file: (List<String>) value) {
                    String temp = fileManager.createDump(file, sceneId);
                    files.add(temp);
                }
                yield files;
            }
            case STRING_ARRAY -> (List<String>) value;
        };
    }

    private void resolveInputs(Node node) {
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

            Object input = node.getInputs().get(key);
            ParameterType requiredType = node.getInputTypes().get(key);

            String sceneId = (String) node.getInputs().get("sceneId");

            if (input instanceof NodeReference) {
                input = resolveReference((NodeReference) input, sceneId);
            }

            // Cast to required type
            try {
                input = castTypes(sceneId, input, requiredType);
            } catch (ClassCastException e) {
                throw new InvalidNodeParameter(
                        "Invalid input parameter '" + key + "' to the node with id " +
                                node.getId() + ": cannot cast " + input.getClass().getSimpleName() +
                                " to " + requiredType + " type"
                );
            }

            resolvedInputs.put(key, input);
        }

        resolvedInputs.put("sceneId", node.getInputs().get("sceneId"));

        node.setInputs(resolvedInputs);
    }

    public void clear(String uuid) {
        if (nodeOutputs.containsKey(uuid)) {
            nodeOutputs.get(uuid).clear();
        }
        if (nodeMap.containsKey(uuid)) {
            nodeMap.get(uuid).clear();
        }
        nodeOutputs.clear();
        nodeMap.clear();
    }
}
