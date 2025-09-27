package com.example.pixel.node_execution.dto;

import lombok.Data;

import java.util.Map;

@Data
public class NodeExecutionResponse {
    private String error;
    private Map<String, Object> outputs;

    @Override
    public String toString() {
        return "NodeExecutionResponse(" +
                "error=" + error + ", " +
                "outputs=" + (outputs == null ? "null" : "Map with " + outputs.size() + " entries" +
                (outputs.isEmpty() ? "" : ", keys: " + String.join(", ", outputs.keySet()))) +
                ")";
    }
}
