package com.example.pixel.graph.model;

import com.example.pixel.node_execution.model.NodeExecution;

import java.util.*;

public class LevelIterator implements Iterator<List<NodeExecution>> {
    private final List<List<NodeExecution>> levels;
    private int currentLevel;

    public LevelIterator(Graph graph) {
        this.levels = new ArrayList<>(graph.getLevelMap().values());
        this.currentLevel = 0;
    }

    @Override
    public boolean hasNext() {
        return currentLevel < levels.size();
    }

    @Override
    public List<NodeExecution> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more levels to traverse");
        }
        return levels.get(currentLevel++);
    }
}

