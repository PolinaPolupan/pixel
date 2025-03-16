package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

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

    /**
     * Returns a map where the key is a node's ID, and the value is a list of
     * all parent IDs for that node.
     */
    public Map<Long, List<Long>> buildParentListMap() {
        Map<Long, List<Long>> parentListMap = new HashMap<>();
        if (nodes == null) {
            return parentListMap;
        }

        for (Node n : nodes) {
            if (n.getOutputs() != null) {
                for (Long childId : n.getOutputs()) {
                    parentListMap
                            .computeIfAbsent(childId, k -> new ArrayList<>())
                            .add(n.getId());
                }
            }
        }

        return parentListMap;
    }
}
