package com.example.pixel.graph.model;

import com.example.pixel.node_execution.model.Node;

import java.util.*;

public class LevelIterator implements Iterator<List<Node>> {
    private final List<List<Node>> levels;
    private int currentLevel;

    public LevelIterator(List<List<Node>> levels) {
        this.levels = levels;
        this.currentLevel = 0;
    }

    @Override
    public boolean hasNext() {
        return currentLevel < levels.size();
    }

    @Override
    public List<Node> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more levels to traverse");
        }
        return levels.get(currentLevel++);
    }
}

