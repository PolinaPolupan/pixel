package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Graph {
    List<Node> nodes;

    /**
     * Creates a BFS iterator starting from the specified node.
     *
     * @param startNodeId ID of the node to start traversal from
     * @return An iterator that traverses nodes in BFS order
     */
    public Iterator<Node> iterator(Long startNodeId) {
        return new GraphIterator(this, startNodeId);
    }
}
