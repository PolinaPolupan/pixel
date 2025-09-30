package com.example.pixel.node.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodePayload {
    private Long id;
    private String type;
    private Integer version;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private Map<String, Object> display;
    private Instant createdAt;
    private Boolean active;
}
