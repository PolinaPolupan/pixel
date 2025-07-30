package com.example.mypixel.model;

import lombok.Data;

import java.util.Map;

@Data
public class NodeConfig {
    private String type;
    private Map<String, ParameterConfig> inputs;
    private Map<String, Object> defaultInputs;
    private Map<String, ParameterConfig> outputs;
    private Map<String, String> display;
}
