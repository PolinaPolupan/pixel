package com.example.pixel.graph;

import com.example.pixel.graph.service.GraphBuilder;
import com.example.pixel.node_execution.model.Node;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream. Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GraphBuilderTopologicalSortTest {

    @Test
    void testTopologicalSort_SimpleLinearGraph() {
        System.out.println("\n=== Test: Simple Linear Graph ===");

        List<Node> nodes = Arrays.asList(
                new Node(3L, "output", Map.of("input", "@node:2:output")),
                new Node(1L, "input", Map. of("value", 10)),
                new Node(2L, "process", Map.of("input", "@node:1:output"))
        );

        printInitialOrder(nodes);

        List<Node> processedNodes = GraphBuilder.setupReferences(nodes);
        Map<Long, Node> nodeMap = GraphBuilder.getNodeMap(processedNodes);
        Map<Node, List<Node>> nodeOutputs = GraphBuilder.mapOutputNodes(processedNodes);
        List<List<Node>> levels = GraphBuilder.buildTopologicalOrder(processedNodes, nodeOutputs, nodeMap);
        List<Node> topologicalOrder = GraphBuilder.getTopologicalOrderFromLevels(levels);

        printTopologicalOrder(topologicalOrder, levels);

        assertEquals(3, topologicalOrder.size());
        assertEquals(1L, topologicalOrder.get(0).getId());
        assertEquals(2L, topologicalOrder.get(1).getId());
        assertEquals(3L, topologicalOrder.get(2).getId());
    }

    @Test
    void testTopologicalSort_DiamondGraph() {
        System.out. println("\n=== Test: Diamond Graph ===");

        List<Node> nodes = Arrays. asList(
                new Node(4L, "output", Map.of(
                        "input1", "@node:2:output",
                        "input2", "@node:3:output"
                )),
                new Node(2L, "process1", Map.of("input", "@node:1:output")),
                new Node(1L, "input", Map.of("value", 10)),
                new Node(3L, "process2", Map.of("input", "@node:1:output"))
        );

        printInitialOrder(nodes);

        List<Node> processedNodes = GraphBuilder.setupReferences(nodes);
        Map<Long, Node> nodeMap = GraphBuilder. getNodeMap(processedNodes);
        Map<Node, List<Node>> nodeOutputs = GraphBuilder.mapOutputNodes(processedNodes);
        List<List<Node>> levels = GraphBuilder.buildTopologicalOrder(processedNodes, nodeOutputs, nodeMap);
        List<Node> topologicalOrder = GraphBuilder.getTopologicalOrderFromLevels(levels);

        printTopologicalOrder(topologicalOrder, levels);

        assertEquals(3, levels.size());
        assertEquals(1, levels.get(0).size());
        assertEquals(2, levels.get(1).size());
        assertEquals(1, levels.get(2).size());

        assertEquals(1L, topologicalOrder.get(0).getId());
        assertEquals(4L, topologicalOrder. get(3).getId());
        assertTrue(topologicalOrder.get(1).getId() == 2L || topologicalOrder.get(1).getId() == 3L);
        assertTrue(topologicalOrder.get(2).getId() == 2L || topologicalOrder.get(2).getId() == 3L);
    }

    @Test
    void testTopologicalSort_ComplexGraph() {
        System.out. println("\n=== Test: Complex Graph ===");

        List<Node> nodes = Arrays.asList(
                new Node(2L, "output", Map.of("input", "@node:1:output")),
                new Node(11L, "combine", Map.of(
                        "files_0", "@node:10:output",
                        "files_1", "@node:0:output"
                )),
                new Node(1L, "gaussian_blur", Map.of(
                        "input", "@node:11:output",
                        "sigmaX", "@node:4:output"
                )),
                new Node(4L, "floor", Map.of("input", 56)),
                new Node(10L, "input_node", Map.of("input", List.of("file. jpg"))),
                new Node(0L, "s3_input", Map.of("conn_id", "my_s3")),
                new Node(12L, "s3_output", Map.of(
                        "conn_id", "my_s3",
                        "input", "@node:1:output"
                ))
        );

        printInitialOrder(nodes);

        List<Node> processedNodes = GraphBuilder.setupReferences(nodes);
        Map<Long, Node> nodeMap = GraphBuilder.getNodeMap(processedNodes);
        Map<Node, List<Node>> nodeOutputs = GraphBuilder. mapOutputNodes(processedNodes);
        List<List<Node>> levels = GraphBuilder.buildTopologicalOrder(processedNodes, nodeOutputs, nodeMap);
        List<Node> topologicalOrder = GraphBuilder.getTopologicalOrderFromLevels(levels);

        printTopologicalOrder(topologicalOrder, levels);

        assertEquals(4, levels.size());

        assertEquals(3, levels.get(0).size());

        assertEquals(1, levels.get(1).size());
        assertEquals(11L, levels.get(1).get(0).getId());

        assertEquals(1, levels.get(2).size());
        assertEquals(1L, levels. get(2).get(0).getId());

        assertEquals(2, levels.get(3).size());
    }

    @Test
    void testTopologicalSort_MultipleRoots() {
        System.out.println("\n=== Test: Multiple Root Nodes ===");

        List<Node> nodes = Arrays.asList(
                new Node(5L, "merge", Map.of(
                        "a", "@node:1:output",
                        "b", "@node:2:output",
                        "c", "@node:3:output"
                )),
                new Node(1L, "root1", Map.of()),
                new Node(3L, "root3", Map.of()),
                new Node(2L, "root2", Map.of())
        );

        printInitialOrder(nodes);

        List<Node> processedNodes = GraphBuilder.setupReferences(nodes);
        Map<Long, Node> nodeMap = GraphBuilder.getNodeMap(processedNodes);
        Map<Node, List<Node>> nodeOutputs = GraphBuilder.mapOutputNodes(processedNodes);
        List<List<Node>> levels = GraphBuilder.buildTopologicalOrder(processedNodes, nodeOutputs, nodeMap);
        List<Node> topologicalOrder = GraphBuilder.getTopologicalOrderFromLevels(levels);

        printTopologicalOrder(topologicalOrder, levels);

        assertEquals(2, levels.size());
        assertEquals(3, levels.get(0).size());
        assertEquals(1, levels.get(1).size());
        assertEquals(5L, topologicalOrder.get(3).getId());
    }

    private void printInitialOrder(List<Node> nodes) {
        System.out.println("\n Initial Order (unsorted):");
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            System.out.printf("  %d. Node %d (type: %s)%n",
                    i + 1, node.getId(), node.getType());
        }
    }

    private void printTopologicalOrder(List<Node> topologicalOrder, List<List<Node>> levels) {
        System.out. println("\n Topological Order (sorted):");
        for (int i = 0; i < topologicalOrder.size(); i++) {
            Node node = topologicalOrder.get(i);
            System.out.printf("  %d. Node %d (type: %s)%n",
                    i + 1, node.getId(), node.getType());
        }

        System.out.println("\n Levels (parallel execution groups):");
        for (int i = 0; i < levels.size(); i++) {
            List<Long> nodeIds = levels.get(i).stream()
                    .map(Node::getId)
                    .collect(Collectors.toList());
            System.out.printf("  Level %d: %s%n", i, nodeIds);
        }
    }
}