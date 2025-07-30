package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.ParamsMap;
import com.example.mypixel.model.Widget;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("Input")
public class InputNode extends Node {

    @JsonCreator
    public InputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "input", Parameter.builder()
                        .type(ParameterType.FILEPATH_ARRAY)
                        .required(true)
                        .widget(Widget.FILE_PICKER)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "input", new HashSet<String>()
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of(
                "output", Parameter.builder()
                        .type(ParameterType.FILEPATH_ARRAY)
                        .required(true)
                        .widget(Widget.LABEL)
                        .build()
        );
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "IO",
                "description", "Input files",
                "color", "#AED581",
                "icon", "InputIcon"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("input");
        return Map.of("output", files);
    }

    @Override
    public void validate() {

    }
}
