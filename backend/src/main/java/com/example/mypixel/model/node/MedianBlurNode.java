package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.FilteringService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("MedianBlur")
public class MedianBlurNode extends Node {

    @Autowired
    private FilteringService filteringService;

    @JsonCreator
    public MedianBlurNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILEPATH_ARRAY.required(),
                "ksize", ParameterType.INT.required()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY);
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
