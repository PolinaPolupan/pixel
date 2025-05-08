package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.Map;

@MyPixelNode("OutputFile")
public class OutputFileNode extends Node {

    @JsonCreator
    public OutputFileNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public String getCategory() {
        return "IO";
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "content", ParameterType.STRING.optional(),
                "filename", ParameterType.STRING.optional()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "content", "",
                "filename", "new.txt"
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "description", "Output to a file",
                "color", "#AED581",
                "icon", "OutputIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        String content = (String) inputs.getOrDefault("content", "");
        String filename = (String) inputs.getOrDefault("filename", "new.txt");

        fileHelper.storeFile(filename, content);

        return Map.of();
    }

    @Override
    public void validate() {

    }
}
