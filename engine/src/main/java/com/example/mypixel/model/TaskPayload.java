package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskPayload {
    private Long id;
    private Long sceneId;
    private TaskStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalNodes;
    private Integer processedNodes;
    private String errorMessage;

    public static TaskPayload fromEntity(Task task) {
        if (task == null) return null;
        return new TaskPayload(
                task.getId(),
                task.getSceneId(),
                task.getStatus(),
                task.getStartTime(),
                task.getEndTime(),
                task.getTotalNodes(),
                task.getProcessedNodes(),
                task.getErrorMessage()
        );
    }
}