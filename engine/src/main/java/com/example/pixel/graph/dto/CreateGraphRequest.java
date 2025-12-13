package com.example.pixel.graph.dto;

import com.example.pixel.node_execution.model.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateGraphRequest {
    private final String id;
    private final String schedule;
    private final List<Node> nodes;
}
