package com.example.pixel.execution_graph.dto;

import com.example.pixel.node.dto.NodePayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateExecutionGraphRequest {
    private List<NodePayload> nodes;
}
