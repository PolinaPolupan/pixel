package com.example.pixel.node.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class NodeConfiguration {
    private String type;
    private Map<String, Object> inputs;
    private Map<String, Object>  outputs;
    private Map<String, Object>  display;
}
