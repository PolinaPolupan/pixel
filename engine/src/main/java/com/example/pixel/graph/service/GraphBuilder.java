package com.example.pixel.graph.service;

import com.example.pixel.node_execution.model.Node;
import com.example.pixel.node_execution.model.NodeReference;

import java.util.*;

public class GraphBuilder {

    public static Map<Long, Node> getNodeMap(List<Node> nodes) {
        Map<Long, Node> nodeMap = new HashMap<>();
        for (Node node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        return nodeMap;
    }

    public static List<Node> setupReferences(List<Node> nodes) {
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

    public static Map<Node, List<Node>> mapOutputNodes(List<Node> nodes) {
        Map<Node, List<Node>> nodeOutputs = new HashMap<>();

        for (Node node : nodes) {
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

        return nodeOutputs;
    }

    public static List<List<Node>> buildTopologicalOrder(
            List<Node> nodes,
            Map<Node, List<Node>> nodeOutputs,
            Map<Long, Node> nodeMap
    ) {
        List<List<Node>> levels = new ArrayList<>();
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

        return levels;
    }

    public static List<Node> getTopologicalOrderFromLevels(List<List<Node>> levels) {
        List<Node> result = new ArrayList<>();
        for (List<Node> level : levels) {
            result.addAll(level);
        }
        return result;
    }
}
