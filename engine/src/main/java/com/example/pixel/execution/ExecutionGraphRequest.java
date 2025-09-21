package com.example.pixel.execution;

import com.example.pixel.node.Node;
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
