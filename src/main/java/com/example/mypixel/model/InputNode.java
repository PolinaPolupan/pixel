package com.example.mypixel.model;

import com.example.mypixel.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InputNode extends Node {

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

    @Autowired
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @Autowired
    public InputNode(
            Long id,
            NodeType type,
            Map<String, Object> inputs
    ) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterTypes> getInputTypes() {
        return Map.of("files", ParameterTypes.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, ParameterTypes> getOutputTypes() {
        return Map.of("files", ParameterTypes.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        List<String> temp = new ArrayList<>();
        Map<String, Object> outputs;

        for (String file: files) {
            temp.add(tempStorageService.createTempFileFromResource(storageService.loadAsResource(file)));
        }

        outputs = Map.of("files", temp);

        return outputs;
    }
}
