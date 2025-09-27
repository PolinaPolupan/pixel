package com.example.pixel.graph_execution.entity;

import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "graph_executions")
public class GraphExecutionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long graphId;

    @Enumerated(EnumType.STRING)
    private GraphExecutionStatus status;

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
