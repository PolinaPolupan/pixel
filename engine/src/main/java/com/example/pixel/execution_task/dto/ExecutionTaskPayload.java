package com.example.pixel.execution_task.dto;

import com.example.pixel.execution_task.entity.ExecutionTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionTaskPayload {
    private Long id;
    private Long graphId;
    private ExecutionTaskStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalNodes;
    private Integer processedNodes;
    private String errorMessage;

    public static ExecutionTaskPayload fromEntity(ExecutionTaskEntity executionTaskEntity) {
        if (executionTaskEntity == null) return null;
        return new ExecutionTaskPayload(
                executionTaskEntity.getId(),
                executionTaskEntity.getGraphId(),
                executionTaskEntity.getStatus(),
                executionTaskEntity.getStartTime(),
                executionTaskEntity.getEndTime(),
                executionTaskEntity.getTotalNodes(),
                executionTaskEntity.getProcessedNodes(),
                executionTaskEntity.getErrorMessage()
        );
    }
}