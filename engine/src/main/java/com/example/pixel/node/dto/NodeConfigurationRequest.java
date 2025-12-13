package com.example.pixel.node.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class NodeConfigurationRequest {
    private String type;
    private Map<String, Object> inputs;
    private Map<String, Object>  outputs;
    private Map<String, Object>  display;
}
