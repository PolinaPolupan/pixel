package com.example.mypixel.model.node;

import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.FilteringService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;


@MyPixelNode("GaussianBlur")
public class GaussianBlurNode extends Node {

    @Autowired
    private FilteringService filteringService;

    @JsonCreator
    public GaussianBlurNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull NodeType type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILENAMES_ARRAY.required(),
                "sizeX", ParameterType.INT.required(),
                "sizeY", ParameterType.INT.optional(),
                "sigmaX", ParameterType.DOUBLE.optional(),
                "sigmaY", ParameterType.DOUBLE.optional()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        Map<String, Object> outputs;

        int sizeX = (int) inputs.get("sizeX");
        int sizeY = (int) inputs.getOrDefault("sizeY", sizeX);
        double sigmaX = (double) inputs.getOrDefault("sigmaX", 0.0);
        double sigmaY = (double) inputs.getOrDefault("sigmaY", 0.0);

        for (String file: files) {
            filteringService.gaussianBlur(file, sizeX, sizeY, sigmaX, sigmaY);
        }

        outputs = Map.of("files", files);

        return outputs;
    }
}
