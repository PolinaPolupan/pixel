package com.example.mypixel.model.node;

import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.FilteringService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;


public class GaussianBlurNode extends Node {

    @Autowired
    private FilteringService filteringService;

    public GaussianBlurNode(Long id, NodeType type, Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILENAMES_ARRAY,
                "sizeX", ParameterType.INT,
                "sizeY", ParameterType.INT,
                "sigmaX", ParameterType.DOUBLE,
                "sigmaY", ParameterType.DOUBLE
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

        int sizeX = (int) inputs.getOrDefault("sizeX", 1);
        int sizeY = (int) inputs.getOrDefault("sizeY", 1);
        double sigmaX = (double) inputs.getOrDefault("sigmaX", 0.0);
        double sigmaY = (double) inputs.getOrDefault("sigmaY", 0.0);

        for (String file: files) {
            filteringService.gaussianBlur(file, sizeX, sizeY, sigmaX, sigmaY);
        }

        outputs = Map.of("files", files);

        return outputs;
    }
}
