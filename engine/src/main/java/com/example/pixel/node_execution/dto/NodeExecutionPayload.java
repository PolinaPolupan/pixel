package com.example.pixel.node_execution.dto;
import com.example.pixel.node_execution.model.NodeExecution;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class NodeExecutionPayload {
    private Long id;
    private String type;
    private Map<String, Object> inputs;

    public NodeExecution toNode() {
        return new NodeExecution(id, type, inputs);
    }
}
