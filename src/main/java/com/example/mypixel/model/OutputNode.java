package com.example.mypixel.model;

import com.example.mypixel.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;


public class OutputNode extends Node {

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

    @Autowired
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    public OutputNode(Long id,
                      NodeType type,
                      Map<String, Object> inputs
    ) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterTypes> getInputTypes() {
        return Map.of(
                "files", ParameterTypes.FILENAMES_ARRAY,
                "prefix", ParameterTypes.STRING
        );
    }

    @Override
    public Map<String, ParameterTypes> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        Map<String, Object> outputs = Map.of();

        for (String file: files) {
            String tempFile = tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource(file));
            String filename = tempStorageService.removeExistingPrefix(tempFile);
            if (inputs.get("prefix") != null) {
                filename = inputs.get("prefix") + "_" + filename;
            }
            storageService.store(tempStorageService.loadAsResource(tempFile), filename);
        }

        return outputs;
    }
}
