package com.example.pixel.execution_graph.entity;

import com.example.pixel.execution_graph.model.ExecutionGraph;
import com.example.pixel.node.dto.NodePayload;
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

    private LocalDateTime lastAccessed;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<NodePayload> nodes;

    // Version for optimistic locking in distributed environments
    @JsonIgnore
    @Version
    private Long version;

    public ExecutionGraph toExecutionGraph() {
        return new ExecutionGraph(id, nodes.stream().map(NodePayload::toNode).toList());
    }
}
