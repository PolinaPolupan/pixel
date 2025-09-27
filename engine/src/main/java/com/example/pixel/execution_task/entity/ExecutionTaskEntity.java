package com.example.pixel.execution_task.entity;

import com.example.pixel.execution_task.dto.ExecutionTaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "execution_tasks")
public class ExecutionTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long graphId;

    @Enumerated(EnumType.STRING)
    private ExecutionTaskStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer totalNodes;
    private Integer processedNodes;

    private String errorMessage;

    // Version for optimistic locking in distributed environments
    @JsonIgnore
    @Version
    private Long version;
}
