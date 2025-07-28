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

@MyPixelNode("BilateralFilter")
public class BilateralFilterNode extends Node {

    @JsonCreator
    public BilateralFilterNode(
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
                        .build(),
                "d", Parameter.builder()
                        .type(ParameterType.INT)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build(),
                "sigmaColor", Parameter.builder()
                        .type(ParameterType.DOUBLE)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build(),
                "sigmaSpace", Parameter.builder()
                        .type(ParameterType.DOUBLE)
                        .required(true)
                        .widget(Widget.INPUT)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "files", new HashSet<String>(),
                "d", 9,
                "sigmaColor", 75.0,
                "sigmaSpace", 75.0
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of(
                "files", Parameter.builder()
                        .type(ParameterType.FILEPATH_ARRAY)
                        .required(true)
                        .widget(Widget.LABEL)
                        .build()
        );
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "Filtering",
                "description", "Applies a bilateral filter to the input image.",
                "color", "#FF8A65",
                "icon", "BlurIcon"
        );
    }

    @SuppressWarnings("unchecked")
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
