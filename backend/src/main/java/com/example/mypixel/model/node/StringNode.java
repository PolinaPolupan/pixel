package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.ParamsMap;
import com.example.mypixel.model.Widget;
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
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "input", Parameter.builder()
                        .type(ParameterType.STRING)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "input", ""
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of("output", Parameter.required(ParameterType.STRING));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "Types",
                "description", "String",
                "color", "#AED581",
                "icon", "StringIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;
        String value = (String) inputs.get("input");
        outputs = Map.of("output", value);
        return outputs;
    }

    @Override
    public void validate() {

    }
}

