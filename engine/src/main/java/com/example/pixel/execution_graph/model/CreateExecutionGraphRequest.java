package com.example.pixel.execution_graph.model;

import com.example.pixel.node.model.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateExecutionGraphRequest {
    private List<Node> nodes;
}
