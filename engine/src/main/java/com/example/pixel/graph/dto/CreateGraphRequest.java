package com.example.pixel.graph.dto;

import com.example.pixel.node_execution.dto.NodeExecutionPayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateGraphRequest {
    private List<NodeExecutionPayload> nodes;
}
