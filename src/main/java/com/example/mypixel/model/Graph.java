package com.example.mypixel.model;

import com.example.mypixel.model.node.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.*;

@Getter
public class Graph {
    List<Node> nodes;

    @JsonCreator
    public Graph(@JsonProperty("nodes") List<Node> nodes) {
        this.nodes = nodes;
    }

    public Iterator<Node> iterator() {
        return new GraphIterator(this);
    }

    public List<Long> getNodeOutputs(Node node) {
        List<Long> outputs = new ArrayList<>();

        for (Node n: nodes) {
            for (Object param: n.getInputs().values()) {
                if (param instanceof NodeReference) {
                    if (((NodeReference) param).getNodeId().equals(node.getId())) outputs.add(n.getId());
                }
            }
        }
        return outputs;
    }
}
