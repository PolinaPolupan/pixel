package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidGraph;
import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.node.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class Graph {
    private final List<Node> nodes;
    @JsonIgnore
    private final List<Node> topologicalOrder = new ArrayList<>();
    @JsonIgnore
    private final Map<Long, Node> nodeMap = new HashMap<>();
    @JsonIgnore
    private final Map<Node, List<Node>> nodeOutputs = new HashMap<>();

    @JsonCreator
    public Graph(@JsonProperty("nodes") List<Node> nodes) {
        this.nodes = nodes;

        // First populate the node map
        for (Node node: nodes) nodeMap.put(node.getId(), node);

        // Then process the nodes
        for (Node node: nodes) {
            mapOutputNodes(node);
            validateNodeInputs(node);
        }

        buildTopologicalOrder();
        verifyGraphIntegrity();
    }

    public Iterator<Node> iterator() {
        return new GraphIterator(this);
    }

    private void mapOutputNodes(Node node) {
        List<Node> dependentNodes = new ArrayList<>();
        for (Node potentialDependent: nodes) {
            for (Object param: potentialDependent.getInputs().values()) {
                if (param instanceof NodeReference) {
                    if (((NodeReference) param).getNodeId().equals(node.getId())) {
                        dependentNodes.add(potentialDependent);
                    }
                }
            }
        }
        nodeOutputs.put(node, dependentNodes);
    }

    private void validateNodeInputs(Node node) {
        for (String paramName: node.getInputTypes().keySet()) {
            // If the user's inputs don't contain one of the parameters
            if (!node.getInputs().containsKey(paramName)) {
                // If it is required - throw an exception
                if (node.getInputTypes().get(paramName).isRequired()) {
                    throw new InvalidNodeParameter("Required input " + paramName
                            + " is not provided for the node with id " + node.getId());
                }
            }

            validateParameterValue(node, paramName);
        }
    }

    private void validateParameterValue(Node node, String paramName) {
        Object paramValue = node.getInputs().get(paramName);
        ParameterType parameterType = node.getInputTypes().get(paramName).getType();

        if (paramValue instanceof NodeReference) {
            validateNodeReference((NodeReference) paramValue);
        }

        if (paramValue instanceof Number) {
            overflowCheck((Number) paramValue, parameterType);
        }
    }

    private void validateNodeReference(NodeReference reference) {
        Long targetNodeId = reference.getNodeId();
        String targetOutputName = reference.getOutputName();

        if (!nodeMap.containsKey(targetNodeId)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id " +
                    targetNodeId + " is not found. Please ensure the node id is correct.");
        }

        if (!nodeMap.get(targetNodeId).getOutputTypes().containsKey(targetOutputName)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id "
                    + targetNodeId + " does not contain output '" + targetOutputName
                    + "'. Available outputs are: " + nodeMap.get(targetNodeId).getOutputTypes().keySet());
        }
    }

    private void verifyGraphIntegrity() {
        // Check for duplicate nodes ids
        Set<Long> seenIds = new HashSet<>();
        List<Long> duplicateIds = new ArrayList<>();

        for (Node node: nodes) {
            if (!seenIds.add(node.getId())) {
                // If we couldn't add to the set, it's a duplicate
                duplicateIds.add(node.getId());
            }
        }

        if (!duplicateIds.isEmpty()) {
            throw new InvalidGraph("Graph contains nodes with duplicate IDs: " + duplicateIds);
        }

        // Check for cycles
        if (topologicalOrder.size() != nodes.size()) {
            throw new InvalidGraph("Graph contains a cycle");
        }

        log.info("Graph validation passed: no duplicate node IDs found");
    }

    private void buildTopologicalOrder() {
        Map<Long, Integer> inDegreeMap = new HashMap<>();
        Queue<Node> zeroInDegreeNodes = new LinkedList<>();

        // Initialize inDegreeMap
        for (Node node: nodes) {
            inDegreeMap.put(node.getId(), 0);
        }

        // Calculate in-degrees
        for (Node node: nodes) {
            for (Node dependent: nodeOutputs.get(node)) {
                inDegreeMap.put(dependent.getId(), inDegreeMap.get(dependent.getId()) + 1);
            }
        }

        // Add nodes with zero in-degree to the queue
        for (Map.Entry<Long, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeNodes.add(nodeMap.get(entry.getKey()));
            }
        }

        // Process nodes with zero in-degree
        while (!zeroInDegreeNodes.isEmpty()) {
            Node current = zeroInDegreeNodes.poll();
            topologicalOrder.add(current);

            for (Node dependent: nodeOutputs.get(current)) {
                int inDegree = inDegreeMap.get(dependent.getId()) - 1;
                inDegreeMap.put(dependent.getId(), inDegree);
                if (inDegree == 0) {
                    zeroInDegreeNodes.add(nodeMap.get(dependent.getId()));
                }
            }
        }
    }

    private void overflowCheck(Number value, ParameterType type) {
        double doubleValue = value.doubleValue();
        String originalValue = value.toString();

        switch (type) {
            case INT -> {
                if (doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
                    throw new InvalidNodeParameter(
                            String.format("Value %s exceeds integer range [%d, %d]",
                                    originalValue, Integer.MIN_VALUE, Integer.MAX_VALUE)
                    );
                }
            }
            case FLOAT -> {
                if (doubleValue > Float.MAX_VALUE || doubleValue < -Float.MAX_VALUE) {
                    throw new InvalidNodeParameter(
                            String.format("Value %s exceeds float range [%s, %s]",
                                    originalValue, -Float.MAX_VALUE, Float.MAX_VALUE)
                    );
                }
            }
            case DOUBLE -> {
                if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
                    throw new InvalidNodeParameter(
                            String.format("Value %s is too large or not a valid number for double representation",
                                    originalValue)
                    );
                }
            }
        }
    }
}