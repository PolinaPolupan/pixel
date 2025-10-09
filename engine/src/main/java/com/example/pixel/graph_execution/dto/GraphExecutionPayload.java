package com.example.pixel.graph_execution.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphExecutionPayload {
    private Long id;
    private String graphId;
    private GraphExecutionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalNodes;
    private Integer processedNodes;
    private String errorMessage;
}