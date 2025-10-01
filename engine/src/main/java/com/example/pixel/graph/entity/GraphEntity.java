package com.example.pixel.graph.entity;

import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.node_execution.model.NodeExecution;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "graphs")
public class GraphEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private String schedule;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<NodeExecution> nodes;

    @OneToMany(mappedBy = "graphId", fetch = FetchType.LAZY)
    private List<GraphExecutionEntity> graphExecutionEntity;

    // Version for optimistic locking in distributed environments
    @JsonIgnore
    @Version
    private Long version;
}
