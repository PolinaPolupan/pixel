package com.example.pixel.graph.dto;

import com.example.pixel.graph.model.Graph;
import com.example.pixel.node.dto.NodePayload;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GraphPayload {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;
    private List<NodePayload> nodes;

    public Graph toGraph() {
        return new Graph(id, nodes.stream().map(NodePayload::toNode).toList());
    }
}
