package com.example.mypixel.model.node;

import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@MyPixelNode("Input")
public class InputNode extends Node {

    @JsonCreator
    public InputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull NodeType type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of("files", ParameterType.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        Map<String, Object> outputs;
        outputs = Map.of("files", files);

        return outputs;
    }
}
