package com.example.pixel.graph.model;

import com.example.pixel.common.exception.InvalidGraphException;
import com.example.pixel.common.exception.InvalidNodeInputException;
import com.example.pixel.node_execution.model.NodeExecution;
import com.example.pixel.node_execution.model.NodeReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class Graph {
    private final Long id;
    private final List<NodeExecution> nodeExecutions;
    private final List<NodeExecution> topologicalOrder = new ArrayList<>();
    private final Map<Long, NodeExecution> nodeMap = new HashMap<>();
    private final Map<NodeExecution, List<NodeExecution>> nodeOutputs = new HashMap<>();

    public Graph(Long id, List<NodeExecution> nodeExecutions) {
        this.id = id;
        this.nodeExecutions = nodeExecutions;

        // First populate the node map
        for (NodeExecution nodeExecution : nodeExecutions) nodeMap.put(nodeExecution.getId(), nodeExecution);

        // Then process the nodes
        for (NodeExecution nodeExecution : nodeExecutions) {
            mapOutputNodes(nodeExecution);
            validateReferences(nodeExecution);
        }

        buildTopologicalOrder();
        verifyGraphIntegrity();
    }

    public Iterator<NodeExecution> iterator() {
        return new GraphIterator(this);
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
            for (NodeExecution dependent: nodeOutputs.get(nodeExecution)) {
                inDegreeMap.put(dependent.getId(), inDegreeMap.get(dependent.getId()) + 1);
            }
        }

        // Add nodes with zero in-degree to the queue
        for (Map.Entry<Long, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeNodeExecutions.add(nodeMap.get(entry.getKey()));
            }
        }

        // Process nodes with zero in-degree
        while (!zeroInDegreeNodeExecutions.isEmpty()) {
            NodeExecution current = zeroInDegreeNodeExecutions.poll();
            topologicalOrder.add(current);

            for (NodeExecution dependent: nodeOutputs.get(current)) {
                int inDegree = inDegreeMap.get(dependent.getId()) - 1;
                inDegreeMap.put(dependent.getId(), inDegree);
                if (inDegree == 0) {
                    zeroInDegreeNodeExecutions.add(nodeMap.get(dependent.getId()));
                }
            }
        }
    }
}