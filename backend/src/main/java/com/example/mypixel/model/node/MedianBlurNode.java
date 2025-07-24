package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("MedianBlur")
public class MedianBlurNode extends Node {

    @JsonCreator
    public MedianBlurNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return Map.of(
                "files", Parameter.required(ParameterType.FILEPATH_ARRAY),
                "ksize", Parameter.required(ParameterType.INT)
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<String>(),
                "ksize", 3
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return Map.of("files", Parameter.required(ParameterType.FILEPATH_ARRAY));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "Filtering",
                "description", "Blurs an image using the specified kernel size",
                "color", "#FF8A65",
                "icon", "BlurIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        Map<String, Object> outputs;

        int ksize = (int) inputs.get("ksize");

        batchProcessor.processBatches(files,
                filepath -> filteringService.medianBlur(filepath, ksize));

        outputs = Map.of("files", files);

        return outputs;
    }

    @Override
    public void validate() {
        int ksize = (int) inputs.get("ksize");

        if (ksize < 2 || ksize % 2 == 0) {
            throw new InvalidNodeParameter("KSize must be greater than 1 and odd");
        }
    }
}
