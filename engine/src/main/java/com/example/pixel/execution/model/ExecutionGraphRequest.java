package com.example.pixel.execution.model;

import com.example.pixel.node.model.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExecutionGraphRequest {
    private Long id;
    private List<Node> nodes;

    public ExecutionGraph toExecutionGraph() {
        return new ExecutionGraph(nodes);
    }
}
