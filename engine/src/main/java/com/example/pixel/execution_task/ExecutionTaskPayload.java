package com.example.pixel.execution_task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionTaskPayload {
    private Long id;
    private Long sceneId;
    private ExecutionTaskStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalNodes;
    private Integer processedNodes;
    private String errorMessage;

    public static ExecutionTaskPayload fromEntity(ExecutionTask executionTask) {
        if (executionTask == null) return null;
        return new ExecutionTaskPayload(
                executionTask.getId(),
                executionTask.getSceneId(),
                executionTask.getStatus(),
                executionTask.getStartTime(),
                executionTask.getEndTime(),
                executionTask.getTotalNodes(),
                executionTask.getProcessedNodes(),
                executionTask.getErrorMessage()
        );
    }
}