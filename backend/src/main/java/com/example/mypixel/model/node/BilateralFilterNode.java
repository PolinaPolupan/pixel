package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.FilteringService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("BilateralFilter")
public class BilateralFilterNode extends Node {

    @Autowired
    private FilteringService filteringService;

    @JsonCreator
    public BilateralFilterNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public String getCategory() {
        return "Filtering";
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILEPATH_ARRAY.required(),
                "d", ParameterType.INT.required(),
                "sigmaColor", ParameterType.DOUBLE.required(),
                "sigmaSpace", ParameterType.DOUBLE.required()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<String>(),
                "d", 9,
                "sigmaColor", 75.0,
                "sigmaSpace", 75.0
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY);
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "description", "Applies a bilateral filter to the input image.",
                "color", "#FF8A65",
                "icon", "BlurIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        Map<String, Object> outputs;

        double sigmaColor = (double) inputs.get("sigmaColor");
        double sigmaSpace = (double) inputs.get("sigmaSpace");
        int d = (int) inputs.get("d");

        batchProcessor.processBatches(files,
                filepath -> filteringService.bilateralFilter(filepath, d, sigmaColor, sigmaSpace));

        outputs = Map.of("files", files);

        return outputs;
    }

    @Override
    public void validate() {
    }
}
