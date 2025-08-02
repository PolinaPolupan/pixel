package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.ParamsMap;
import com.example.mypixel.model.Widget;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@MyPixelNode("ResNet50")
public class ResNet50Node extends Node {

    @JsonCreator
    public ResNet50Node(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "files", Parameter.builder()
                        .type(ParameterType.FILEPATH_ARRAY)
                        .required(true)
                        .widget(Widget.LABEL)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "files", new HashSet<String>()
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of(
                "json",
                Parameter.required(ParameterType.STRING));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "ML",
                "description", "Run ResNet50 on images",
                "color", "#81C784",
                "icon", "ResNet50Icon"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> exec() {
        HashMap<String, Object> outputs = new HashMap<>();
        HashSet<String> files = (HashSet<String>) inputs.get("files");

        return outputs;
    }

    @Override
    public void validate() {
    }
}