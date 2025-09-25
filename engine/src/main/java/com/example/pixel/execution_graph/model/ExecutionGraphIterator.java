package com.example.pixel.execution_graph.model;

import com.example.pixel.node.model.Node;

import java.util.*;

public class ExecutionGraphIterator implements Iterator<Node> {
    private final List<Node> topologicalOrder;
    private int currentIndex;

    public ExecutionGraphIterator(ExecutionGraph executionGraph) {
        this.topologicalOrder = executionGraph.getTopologicalOrder();
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
