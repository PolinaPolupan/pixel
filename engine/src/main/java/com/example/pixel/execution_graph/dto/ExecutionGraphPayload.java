package com.example.pixel.execution_graph.dto;

import com.example.pixel.execution_graph.model.ExecutionGraph;
import com.example.pixel.node.dto.NodePayload;
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
    private List<NodePayload> nodes;

    public ExecutionGraph toExecutionGraph() {
        return new ExecutionGraph(id, nodes.stream().map(NodePayload::toNode).toList());
    }
}
