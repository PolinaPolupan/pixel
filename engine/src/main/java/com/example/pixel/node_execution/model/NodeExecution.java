package com.example.pixel.node_execution.model;

import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
public class NodeExecution {
    private Long id;
    private String type;
    private Map<String, Object> inputs;

    public NodeExecution(@NonNull Long id, @NonNull String type, @NonNull Map<String, Object> inputs) {
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