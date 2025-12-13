package com.example.pixel.graph.dto;

import com.example.pixel.node_execution.model.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GraphDto {
    private final String id;
    private final String schedule;
    private final List<Node> nodes;
}
