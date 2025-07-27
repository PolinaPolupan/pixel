package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.ParamsMap;
import com.example.mypixel.model.Widget;
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
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "input", Parameter.builder()
                        .type(ParameterType.DOUBLE)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build()
        );
    }
    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "input", 0.0
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of("output", Parameter.required(ParameterType.DOUBLE));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "Math",
                "description", "Returns the largest integer less than or equal to the input number.",
                "color", "#BA68C8",
                "icon", "FloorIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;
        double number = (double) inputs.get("input");
        outputs = Map.of("output", Math.floor(number));
        return outputs;
    }

    @Override
    public void validate() {

    }
}

