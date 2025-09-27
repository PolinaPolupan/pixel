package com.example.pixel.graph_execution.dto;

import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphExecutionPayload {
    private Long id;
    private Long graphId;
    private GraphExecutionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalNodes;
    private Integer processedNodes;
    private String errorMessage;

    public static GraphExecutionPayload fromEntity(GraphExecutionEntity graphExecutionEntity) {
        if (graphExecutionEntity == null) return null;
        return new GraphExecutionPayload(
                graphExecutionEntity.getId(),
                graphExecutionEntity.getGraphId(),
                graphExecutionEntity.getStatus(),
                graphExecutionEntity.getStartTime(),
                graphExecutionEntity.getEndTime(),
                graphExecutionEntity.getTotalNodes(),
                graphExecutionEntity.getProcessedNodes(),
                graphExecutionEntity.getErrorMessage()
        );
    }
}