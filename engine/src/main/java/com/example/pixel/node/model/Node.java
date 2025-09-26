package com.example.pixel.node.model;

import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
public class Node {
    private Long id;
    private String type;
    private Map<String, Object> inputs;

    public Node(@NonNull Long id, @NonNull String type, @NonNull Map<String, Object> inputs) {
        this.id = id;
        this.type = type;
        this.inputs = inputs;

        for (Map.Entry<String, Object> input: inputs.entrySet()) {
            Object value = input.getValue();
            if (value instanceof String && ((String) value).startsWith("@node:")) {
                this.inputs.put(input.getKey(), new NodeReference((String) value));
            }
        }
    }
}