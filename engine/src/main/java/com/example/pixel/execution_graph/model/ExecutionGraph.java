package com.example.pixel.execution_graph.model;

import com.example.pixel.common.exception.InvalidGraphException;
import com.example.pixel.common.exception.InvalidNodeInputException;
import com.example.pixel.node.model.Node;
import com.example.pixel.node.model.NodeReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class ExecutionGraph {
    private final List<Node> nodes;
    private final List<Node> topologicalOrder = new ArrayList<>();
    private final Map<Long, Node> nodeMap = new HashMap<>();
    private final Map<Node, List<Node>> nodeOutputs = new HashMap<>();

    public ExecutionGraph(List<Node> nodes) {
        this.nodes = nodes;

        // First populate the node map
        for (Node node: nodes) nodeMap.put(node.getId(), node);

        // Then process the nodes
        for (Node node: nodes) {
            mapOutputNodes(node);
            validateReferences(node);
        }

        buildTopologicalOrder();
        verifyGraphIntegrity();
    }

    public Iterator<Node> iterator() {
        return new ExecutionGraphIterator(this);
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

        for (Node node: nodes) {
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
}