package com.example.pixel.graph.model;

import com.example.pixel.common.exception.InvalidGraphException;
import com.example.pixel.common.exception.InvalidNodeInputException;
import com.example.pixel.node_execution.model.NodeExecution;
import com.example.pixel.node_execution.model.NodeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Graph {
    private final List<NodeExecution> nodeExecutions;
    private final List<NodeExecution> topologicalOrder = new ArrayList<>();
    private final List<List<NodeExecution>> levels = new ArrayList<>();
    private final Map<Long, NodeExecution> nodeMap = new HashMap<>();
    private final Map<NodeExecution, List<NodeExecution>> nodeOutputs = new HashMap<>();

    public Graph(List<NodeExecution> nodeExecutions) {
        this.nodeExecutions = setupReferences(nodeExecutions);

        for (NodeExecution nodeExecution : this.nodeExecutions) {
            nodeMap.put(nodeExecution.getId(), nodeExecution);
        }

        for (NodeExecution nodeExecution : this.nodeExecutions) {
            mapOutputNodes(nodeExecution);
            validateReferences(nodeExecution);
        }

        buildTopologicalOrder();
        verifyGraphIntegrity();
    }

    public Iterator<List<NodeExecution>> levelIterator() {
        return new LevelIterator(levels);
    }

    public Iterator<NodeExecution> nodeIterator() {
        return new NodeIterator(topologicalOrder);
    }

    private List<NodeExecution> setupReferences(List<NodeExecution> nodeExecutions) {
        List<NodeExecution> result = new ArrayList<>();
        for (NodeExecution nodeExecution : nodeExecutions) {
            Map<String, Object> inputsCopy = new HashMap<>();
            for (Map.Entry<String, Object> input : nodeExecution.getInputs().entrySet()) {
                Object value = input.getValue();
                if (value instanceof String && ((String) value).startsWith("@node:")) {
                    inputsCopy.put(input.getKey(), new NodeReference((String) value));
                } else {
                    inputsCopy.put(input.getKey(), value);
                }
            }
            result.add(new NodeExecution(nodeExecution.getId(), nodeExecution.getType(), inputsCopy));
        }
        return result;
    }


    private void mapOutputNodes(NodeExecution nodeExecution) {
        List<NodeExecution> dependentNodeExecutions = new ArrayList<>();
        for (NodeExecution potentialDependent: nodeExecutions) {
            for (Object param: potentialDependent.getInputs().values()) {
                if (param instanceof NodeReference) {
                    if (((NodeReference) param).getNodeId().equals(nodeExecution.getId())) {
                        dependentNodeExecutions.add(potentialDependent);
                    }
                }
            }
        }
        nodeOutputs.put(nodeExecution, dependentNodeExecutions);
    }

    private void validateReferences(NodeExecution nodeExecution) {
        for (String paramName: nodeExecution.getInputs().keySet()) {
            Object paramValue = nodeExecution.getInputs().get(paramName);

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

        for (NodeExecution nodeExecution : nodeExecutions) {
            if (!seenIds.add(nodeExecution.getId())) {
                // If we couldn't add to the set, it's a duplicate
                duplicateIds.add(nodeExecution.getId());
            }
        }

        if (!duplicateIds.isEmpty()) {
            throw new InvalidGraphException("Graph contains nodes with duplicate IDs: " + duplicateIds);
        }

        // Check for cycles
        if (topologicalOrder.size() != nodeExecutions.size()) {
            throw new InvalidGraphException("Graph contains a cycle");
        }

        log.info("Graph validation passed: no duplicate node IDs found");
    }

    private void buildTopologicalOrder() {
        Map<Long, Integer> inDegreeMap = new HashMap<>();
        Queue<NodeExecution> zeroInDegreeNodeExecutions = new LinkedList<>();

        // Initialize inDegreeMap
        for (NodeExecution nodeExecution : nodeExecutions) {
            inDegreeMap.put(nodeExecution.getId(), 0);
        }

        // Calculate in-degrees
        for (NodeExecution nodeExecution : nodeExecutions) {
            for (NodeExecution dependent : nodeOutputs.getOrDefault(nodeExecution, List.of())) {
                inDegreeMap.put(dependent.getId(), inDegreeMap.get(dependent.getId()) + 1);
            }
        }

        // Add nodes with zero in-degree to the queue
        for (Map.Entry<Long, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeNodeExecutions.add(nodeMap.get(entry.getKey()));
            }
        }

        while (!zeroInDegreeNodeExecutions.isEmpty()) {
            int size = zeroInDegreeNodeExecutions.size();
            List<NodeExecution> sameLevelNodes = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                NodeExecution current = zeroInDegreeNodeExecutions.poll();
                sameLevelNodes.add(current);
                topologicalOrder.add(current);

                for (NodeExecution dependent : nodeOutputs.getOrDefault(current, List.of())) {
                    int inDegree = inDegreeMap.get(dependent.getId()) - 1;
                    inDegreeMap.put(dependent.getId(), inDegree);
                    if (inDegree == 0) {
                        zeroInDegreeNodeExecutions.add(nodeMap.get(dependent.getId()));
                    }
                }
            }

            levels.add(sameLevelNodes);
        }
    }
}