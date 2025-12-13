package com.example.pixel.node_execution.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class Node {
    private Long id;
    private String type;
    private Map<String, Object> inputs;
}