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

        for (Node n: nodes) {
            for (Long childId: getNodeOutputs(n)) {
                parentListMap
                    .computeIfAbsent(childId, k -> new ArrayList<>())
                    .add(n.getId());
            }
        }

        return parentListMap;
    }

    public List<Long> getNodeOutputs(Node node) {
        List<Long> outputs = new ArrayList<>();

        for (Node n: nodes) {
            for (Object param: n.getParams().values()) {
                if (param instanceof NodeReference) {
                    if (((NodeReference) param).getNodeId().equals(node.getId())) outputs.add(n.getId());
                }
            }
        }
        return outputs;
    }
}
