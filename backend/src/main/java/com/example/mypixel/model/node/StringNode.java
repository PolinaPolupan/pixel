package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.Map;

@MyPixelNode("String")
public class StringNode extends Node {

    @JsonCreator
    public StringNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of("value", ParameterType.STRING.required());
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("value", ParameterType.STRING);
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;
        String value = (String) inputs.get("value");
        outputs = Map.of("value", value);
        return outputs;
    }

    @Override
    public void validate() {

    }
}

