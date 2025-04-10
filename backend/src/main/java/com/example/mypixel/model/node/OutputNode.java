package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.StorageService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;


@MyPixelNode("Output")
public class OutputNode extends Node {

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

    @Autowired
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @JsonCreator
    public OutputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILENAMES_ARRAY.required(),
                "prefix", ParameterType.STRING.optional()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        Map<String, Object> outputs = Map.of();

        for (String file: files) {
            String filename = tempStorageService.removeExistingPrefix(file);
            if (inputs.get("prefix") != null) {
                filename = inputs.get("prefix") + "_" + filename;
            }
            storageService.store(tempStorageService.loadAsResource(file), filename);
        }

        return outputs;
    }

    @Override
    public void validate() {

    }
}
