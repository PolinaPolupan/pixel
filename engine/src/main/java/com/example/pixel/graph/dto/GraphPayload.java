package com.example.pixel.graph.dto;

import com.example.pixel.node_execution.model.NodeExecution;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GraphPayload {
    private final Long id;
    private final List<NodeExecution> nodeExecutions;
}
