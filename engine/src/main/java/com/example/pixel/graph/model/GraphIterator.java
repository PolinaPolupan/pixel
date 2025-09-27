package com.example.pixel.graph.model;

import com.example.pixel.node.model.Node;

import java.util.*;

public class GraphIterator implements Iterator<Node> {
    private final List<Node> topologicalOrder;
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
    public Node next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes to traverse");
        }

        return topologicalOrder.get(currentIndex++);
    }
}
