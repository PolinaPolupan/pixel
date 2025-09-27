package com.example.pixel.node.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
    name = "nodes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"nodeType", "version"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String nodeType;

    @Column(nullable = false)
    private Integer version;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String inputsConfig;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String outputsConfig;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String displayConfig;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NodeExecutionEntity> executions;
}

