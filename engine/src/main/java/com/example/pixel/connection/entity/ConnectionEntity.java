package com.example.pixel.connection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "connections")
public class ConnectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String connId;
    private String connType;
    private String host;
    private String schema;

    @Column(columnDefinition = "BYTEA")
    private byte[] login;

    @Column(columnDefinition = "BYTEA")
    private byte[] password;

    private int port;
    private String extra;

    @JsonIgnore
    @Version
    private Long version;
}
