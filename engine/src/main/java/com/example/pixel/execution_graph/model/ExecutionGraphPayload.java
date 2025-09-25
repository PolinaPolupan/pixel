package com.example.pixel.execution_graph.model;

import com.example.pixel.node.model.Node;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ExecutionGraphPayload {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;
    private List<Node> nodes;

    public ExecutionGraph toExecutionGraph() {
        return new ExecutionGraph(nodes);
    }
}
