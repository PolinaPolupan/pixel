package com.example.pixel.node_execution.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeExecutionPayload {
    private Long id;
    private Long graphExecutionId;
    private NodeStatus status;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private Instant startedAt;
    private Instant finishedAt;
    private String errorMessage;
}
