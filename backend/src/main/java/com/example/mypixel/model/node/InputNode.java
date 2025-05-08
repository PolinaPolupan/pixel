package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
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
    public Map<String, ParameterType> getInputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY.required());
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<String>()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY);
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "IO",
                "description", "Input files",
                "color", "#AED581",
                "icon", "InputIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        return Map.of("files", files);
    }

    @Override
    public void validate() {

    }
}
