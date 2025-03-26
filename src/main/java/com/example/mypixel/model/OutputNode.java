package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidNodeParameter;
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
    public Map<String, InputTypes> getInputTypes() {
        return Map.of(
                "files", InputTypes.STRING_ARRAY,
                "prefix", InputTypes.STRING
        );
    }

    @Override
    public Map<String, Object> exec(Map<String, Object> inputs) {
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

    @Override
    public void validateInputs() {
        String filename = (String) getInputs().get("filename");
        if (filename == null) {
            throw new InvalidNodeParameter("Invalid node parameter: file cannot be null");
        }
    }
}
