package com.example.pixel.graph.dto;

import com.example.pixel.node.dto.NodePayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateGraphRequest {
    private List<NodePayload> nodes;
}
