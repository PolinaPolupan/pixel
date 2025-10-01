package com.example.pixel.node_execution.entity;

import com.example.pixel.node_execution.dto.NodeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "node_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long graphExecutionId;

    @Enumerated(EnumType.STRING)
    private NodeStatus status;

    @Lob
    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> inputs;

    @Lob
    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> outputs;

    private Instant startedAt;
    private Instant finishedAt;
    private String errorMessage;
}

