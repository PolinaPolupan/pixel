package com.example.pixel.node.model;
import lombok.Data;

import java.util.Map;

@Data
public class NodePayload {
    private Long id;
    private String type;
    private Map<String, Object> inputs;

    public Node toNode() {
        return new Node(id, type, inputs);
    }
}
