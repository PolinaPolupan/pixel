package com.example.mypixel.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ParameterConfig {
    private String type;
    private boolean required;
    private String widget;

    public static Map<String, Parameter> castConfigsToParameters(Map<String, ParameterConfig> configMap) {
        Map<String, Parameter> result = new HashMap<>();
        for (Map.Entry<String, ParameterConfig> entry : configMap.entrySet()) {
            ParameterConfig cfg = entry.getValue();
            ParameterType type = ParameterType.valueOf(cfg.getType()); // Assumes string matches enum name
            Widget widget = Widget.valueOf(cfg.getWidget());
            Parameter param = Parameter.builder()
                    .type(type)
                    .required(cfg.isRequired())
                    .widget(widget)
                    .build();
            result.put(entry.getKey(), param);
        }
        return result;
    }
}