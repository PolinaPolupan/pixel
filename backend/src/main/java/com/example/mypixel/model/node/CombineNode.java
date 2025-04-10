package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@MyPixelNode("Combine")
public class CombineNode extends Node {

    @JsonCreator
    public CombineNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files_0", ParameterType.FILENAMES_ARRAY.required(),
                "files_1", ParameterType.FILENAMES_ARRAY.optional(),
                "files_2", ParameterType.FILENAMES_ARRAY.optional(),
                "files_3", ParameterType.FILENAMES_ARRAY.optional(),
                "files_4", ParameterType.FILENAMES_ARRAY.optional(),
                "files_5", ParameterType.FILENAMES_ARRAY.optional(),
                "files_6", ParameterType.FILENAMES_ARRAY.optional(),
                "files_7", ParameterType.FILENAMES_ARRAY.optional(),
                "files_8", ParameterType.FILENAMES_ARRAY.optional(),
                "files_9", ParameterType.FILENAMES_ARRAY.optional()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            if (inputs.containsKey("files_" + i) && inputs.get("files_" + i) != null) {
                List<String> f = (List<String>) inputs.get("files_" + i);
                files.addAll(f);
            }
        }

        Map<String, Object> outputs;
        outputs = Map.of("files", files);

        return outputs;
    }

    @Override
    public void validate() {

    }
}
