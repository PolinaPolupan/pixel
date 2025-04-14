package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;


@MyPixelNode("Output")
public class OutputNode extends Node {

    @JsonCreator
    public OutputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILEPATH_ARRAY.required(),
                "prefix", ParameterType.STRING.optional()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        String prefix = (String) inputs.getOrDefault("prefix", null);

        Map<String, Object> outputs = Map.of();

        for (String filepath: files) {
            fileHelper.storeToOutput(filepath, prefix);
        }

        return outputs;
    }

    @Override
    public void validate() {

    }
}
