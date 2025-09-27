package com.example.pixel.node.entity;

import com.example.pixel.node.dto.NodeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "node_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeExecutionEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id")
    private NodeEntity node;

    @Enumerated(EnumType.STRING)
    private NodeStatus status;

    @Lob
    private String inputValues;

    @Lob
    private String outputValues;

    private Instant startedAt;
    private Instant finishedAt;
}

