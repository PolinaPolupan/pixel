package com.example.mypixel.model.node;

import com.example.mypixel.model.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import java.util.Map;

@MyPixelNode("Vector2D")
public class Vector2DNode extends Node {

    @JsonCreator
    public Vector2DNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "x", Parameter.builder()
                        .type(ParameterType.DOUBLE)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build(),
                "y", Parameter.builder()
                        .type(ParameterType.DOUBLE)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "x", 0.0,
                "y", 0.0
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of("vector2D", Parameter.required(ParameterType.VECTOR2D));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "Types",
                "description", "Creates a 2D vector",
                "color", "#FF8A65",
                "icon", "Vector2DIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        Number x = (Number) inputs.get("x");
        Number y = (Number) inputs.get("y");

        Vector2D<Number> vector2D = new Vector2D<>(x, y);

        return Map.of("vector2D", vector2D);
    }

    @Override
    public void validate() {
    }
}
