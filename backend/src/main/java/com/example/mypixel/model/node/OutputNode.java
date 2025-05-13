package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
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
    public Map<String, Parameter> getInputTypes() {
        return Map.of(
                "files", Parameter.required(ParameterType.FILEPATH_ARRAY),
                "prefix", Parameter.optional(ParameterType.STRING),
                "folder", Parameter.optional(ParameterType.STRING)
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<>(),
                "prefix", "",
                "folder", ""
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "IO",
                "description", "Output files to a folder",
                "color", "#AED581",
                "icon", "OutputIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        String prefix = (String) inputs.getOrDefault("prefix", null);
        String folder = (String) inputs.getOrDefault("folder", null);

        Map<String, Object> outputs = Map.of();

        batchProcessor.processBatches(files,
                filepath -> fileHelper.storeToOutput(filepath, folder, prefix));

        return outputs;
    }

    @Override
    public void validate() {

    }
}
