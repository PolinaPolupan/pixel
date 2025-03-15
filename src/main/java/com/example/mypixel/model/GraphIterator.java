package com.example.mypixel.model;

import java.util.*;

/**
 * An iterator that performs breadth-first traversal of a Graph.
 */
public class GraphIterator implements Iterator<Node> {
    private final Queue<Node> queue;
    private final Set<Long> visited;
    private final Map<Long, Node> nodeMap;

    /**
     * Creates a new BFS iterator starting from the specified node.
     *
     * @param graph The graph to traverse
     * @param startNodeId The ID of the node to start traversal from
     */
    public GraphIterator(Graph graph, Long startNodeId) {
        this.queue = new LinkedList<>();
        this.visited = new HashSet<>();
        this.nodeMap = new HashMap<>();

        if (graph == null || graph.getNodes() == null) {
            return;
        }

        // Build a map for quick node lookup
        for (Node node : graph.getNodes()) {
            nodeMap.put(node.getId(), node);
        }

        // Initialize with the start node
        Node startNode = nodeMap.get(startNodeId);
        if (startNode != null) {
            queue.add(startNode);
            visited.add(startNodeId);
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public Node next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes to traverse");
        }

        Node current = queue.poll();

        // Add all unvisited connected nodes to the queue
        assert current != null;
        if (current.getOutputs() != null) {
            for (Long inputId : current.getOutputs()) {
                if (!visited.contains(inputId) && nodeMap.containsKey(inputId)) {
                    Node inputNode = nodeMap.get(inputId);
                    queue.add(inputNode);
                    visited.add(inputId);
                }
            }
        }

        return current;
    }
}
