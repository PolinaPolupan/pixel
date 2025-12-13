package com.example.pixel.graph.model;

import com.example.pixel.common.exception.InvalidGraphException;
import com.example.pixel.common.exception.InvalidNodeInputException;
import com.example.pixel.node_execution.model.Node;
import com.example.pixel.node_execution.model.NodeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Graph {
    private final List<Node> nodes;
    private final List<Node> topologicalOrder = new ArrayList<>();
    private final List<List<Node>> levels = new ArrayList<>();
    private final Map<Long, Node> nodeMap = new HashMap<>();
    private final Map<Node, List<Node>> nodeOutputs = new HashMap<>();

    public Graph(List<Node> nodes) {
        this.nodes = setupReferences(nodes);

        for (Node node : this.nodes) {
            nodeMap.put(node.getId(), node);
        }

        for (Node node : this.nodes) {
            mapOutputNodes(node);
            validateReferences(node);
        }

        buildTopologicalOrder();
        verifyGraphIntegrity();
    }

    public Iterator<List<Node>> levelIterator() {
        return new LevelIterator(levels);
    }

    public Iterator<Node> nodeIterator() {
        return new NodeIterator(topologicalOrder);
    }

    private List<Node> setupReferences(List<Node> nodes) {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            Map<String, Object> inputsCopy = new HashMap<>();
            for (Map.Entry<String, Object> input : node.getInputs().entrySet()) {
                Object value = input.getValue();
                if (value instanceof String && ((String) value).startsWith("@node:")) {
                    inputsCopy.put(input.getKey(), new NodeReference((String) value));
                } else {
                    inputsCopy.put(input.getKey(), value);
                }
            }
            result.add(new Node(node.getId(), node.getType(), inputsCopy));
        }
        return result;
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

    private void validateReferences(Node node) {
        for (String paramName: node.getInputs().keySet()) {
            Object paramValue = node.getInputs().get(paramName);

            if (paramValue instanceof NodeReference) {
                Long targetNodeId = ((NodeReference) paramValue).getNodeId();

                if (!nodeMap.containsKey(targetNodeId)) {
                    throw new InvalidNodeInputException("Invalid node reference: Node with id " +
                            targetNodeId + " is not found. Please ensure the node id is correct.");
                }
            }
        }
    }

    private void verifyGraphIntegrity() {
        // Check for duplicate nodes ids
        Set<Long> seenIds = new HashSet<>();
        List<Long> duplicateIds = new ArrayList<>();

        for (Node node : nodes) {
            if (!seenIds.add(node.getId())) {
                // If we couldn't add to the set, it's a duplicate
                duplicateIds.add(node.getId());
            }
        }

        if (!duplicateIds.isEmpty()) {
            throw new InvalidGraphException("Graph contains nodes with duplicate IDs: " + duplicateIds);
        }

        // Check for cycles
        if (topologicalOrder.size() != nodes.size()) {
            throw new InvalidGraphException("Graph contains a cycle");
        }

        log.info("Graph validation passed: no duplicate node IDs found");
    }

    private void buildTopologicalOrder() {
        Map<Long, Integer> inDegreeMap = new HashMap<>();
        Queue<Node> zeroInDegreeNodes = new LinkedList<>();

        // Initialize inDegreeMap
        for (Node node : nodes) {
            inDegreeMap.put(node.getId(), 0);
        }

        // Calculate in-degrees
        for (Node node : nodes) {
            for (Node dependent : nodeOutputs.getOrDefault(node, List.of())) {
                inDegreeMap.put(dependent.getId(), inDegreeMap.get(dependent.getId()) + 1);
            }
        }

        // Add nodes with zero in-degree to the queue
        for (Map.Entry<Long, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeNodes.add(nodeMap.get(entry.getKey()));
            }
        }

        while (!zeroInDegreeNodes.isEmpty()) {
            int size = zeroInDegreeNodes.size();
            List<Node> sameLevelNodes = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                Node current = zeroInDegreeNodes.poll();
                sameLevelNodes.add(current);
                topologicalOrder.add(current);

                for (Node dependent : nodeOutputs.getOrDefault(current, List.of())) {
                    int inDegree = inDegreeMap.get(dependent.getId()) - 1;
                    inDegreeMap.put(dependent.getId(), inDegree);
                    if (inDegree == 0) {
                        zeroInDegreeNodes.add(nodeMap.get(dependent.getId()));
                    }
                }
            }

            levels.add(sameLevelNodes);
        }
    }
}