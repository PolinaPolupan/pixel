package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidGraph;
import com.example.mypixel.model.node.Node;

import java.util.*;


/**
 * An iterator that performs topological sort traversal of a Graph.
 */
public class GraphIterator implements Iterator<Node> {
    private final List<Node> topologicalOrder;
    private int currentIndex;
    private final Graph graph;

    /**
     * Creates a new topological sort iterator starting from the specified graph.
     *
     * @param graph The graph to traverse
     */
    public GraphIterator(Graph graph) {
        this.graph = graph;
        this.topologicalOrder = new ArrayList<>();
        this.currentIndex = 0;

        if (graph == null || graph.getNodes() == null) {
            return;
        }

        performTopologicalSort();
    }

    private void performTopologicalSort() {
        Map<Long, Node> nodeMap = new HashMap<>();
        Map<Long, Integer> inDegreeMap = new HashMap<>();
        Queue<Node> zeroInDegreeQueue = new LinkedList<>();

        // Initialize nodeMap and inDegreeMap
        for (Node node : graph.getNodes()) {
            nodeMap.put(node.getId(), node);
            inDegreeMap.put(node.getId(), 0);
        }

        // Calculate in-degrees
        for (Node node : graph.getNodes()) {
            for (Node n : graph.getNodeOutputs(node)) {
                inDegreeMap.put(n.getId(), inDegreeMap.get(n.getId()) + 1);
            }
        }

        // Add nodes with zero in-degree to the queue
        for (Map.Entry<Long, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeQueue.add(nodeMap.get(entry.getKey()));
            }
        }

        // Process nodes with zero in-degree
        while (!zeroInDegreeQueue.isEmpty()) {
            Node current = zeroInDegreeQueue.poll();
            topologicalOrder.add(current);

            for (Node node : graph.getNodeOutputs(current)) {
                int inDegree = inDegreeMap.get(node.getId()) - 1;
                inDegreeMap.put(node.getId(), inDegree);
                if (inDegree == 0) {
                    zeroInDegreeQueue.add(nodeMap.get(node.getId()));
                }
            }
        }

        // Check for cycles (if not all nodes are processed)
        if (topologicalOrder.size() != graph.getNodes().size()) {
            throw new InvalidGraph("Graph contains a cycle");
        }
    }

    @Override
    public boolean hasNext() {
        return currentIndex < topologicalOrder.size();
    }

    @Override
    public Node next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes to traverse");
        }

        return topologicalOrder.get(currentIndex++);
    }
}
