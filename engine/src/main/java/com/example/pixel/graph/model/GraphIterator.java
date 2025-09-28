package com.example.pixel.graph.model;

import com.example.pixel.node_execution.model.NodeExecution;

import java.util.*;

public class GraphIterator implements Iterator<NodeExecution> {
    private final List<NodeExecution> topologicalOrder;
    private int currentIndex;

    public GraphIterator(Graph graph) {
        this.topologicalOrder = graph.getTopologicalOrder();
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < topologicalOrder.size();
    }

    @Override
    public NodeExecution next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes to traverse");
        }

        return topologicalOrder.get(currentIndex++);
    }
}
