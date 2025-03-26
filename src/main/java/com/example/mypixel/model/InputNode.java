package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidNodeParameter;
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
    public Map<String, InputTypes> getInputTypes() {
        return Map.of("files", InputTypes.STRING_ARRAY);
    }

    @Override
    public Map<String, Object> exec(Map<String, Object> inputs) {
        List<String> files = (List<String>) inputs.get("files");
        List<String> temp = new ArrayList<>();
        Map<String, Object> outputs;

        for (String file: files) {
            temp.add(tempStorageService.createTempFileFromResource(storageService.loadAsResource(file)));
        }

        outputs = Map.of("files", temp);

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
