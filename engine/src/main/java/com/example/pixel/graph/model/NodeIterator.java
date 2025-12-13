package com.example.pixel.graph.model;


import com.example.pixel.node_execution.model.Node;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NodeIterator implements Iterator<Node> {
    private final List<Node> topologicalOrder;
    private int currentIndex;

    public NodeIterator(List<Node> topologicalOrder) {
        this.topologicalOrder = topologicalOrder;
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