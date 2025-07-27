package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("Blur")
public class BlurNode extends Node {

    @JsonCreator
    public BlurNode(
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
                "ksize", Parameter.builder()
                        .type(ParameterType.VECTOR2D)
                        .required(true)
                        .widget(Widget.LABEL)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "files", new HashSet<String>(),
                "ksize", new Vector2D<>(3, 3)
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of("files", Parameter.required(ParameterType.FILEPATH_ARRAY));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "Filtering",
                "description", "Blurs an image using the specified kernel size",
                "color", "#FF8A65",
                "icon", "BlurIcon"

        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        Map<String, Object> outputs;

        Vector2D<Number> ksize = (Vector2D<Number>) inputs.get("ksize");

        batchProcessor.processBatches(files,
                filepath -> filteringService.blur(filepath, ksize));

        outputs = Map.of("files", files);

        return outputs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void validate() {
        Vector2D<Number> ksize = (Vector2D<Number>) inputs.get("ksize");
        Integer ksizeX = ksize.getX().intValue();
        Integer ksizeY = ksize.getY().intValue();

        if (ksizeX < 1 || ksizeY < 1) {
            throw new InvalidNodeParameter("KSize must be greater than 0");
        }
    }
}
