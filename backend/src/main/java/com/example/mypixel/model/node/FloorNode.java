package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.Map;

@MyPixelNode("Floor")
public class FloorNode extends Node {

    @JsonCreator
    public FloorNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }


    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of("number", ParameterType.DOUBLE.required());
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "number", 0.0
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("number", ParameterType.DOUBLE);
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "Math",
                "description", "Returns the largest integer less than or equal to the input number.",
                "color", "#BA68C8",
                "icon", "FloorIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;
        double number = (double) inputs.get("number");
        outputs = Map.of("number", Math.floor(number));
        return outputs;
    }

    @Override
    public void validate() {

    }
}

