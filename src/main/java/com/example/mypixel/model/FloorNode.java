package com.example.mypixel.model;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class FloorNode extends Node {

    @Autowired
    public FloorNode(
            Long id,
            NodeType type,
            Map<String, Object> inputs
    ) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, InputTypes> getInputTypes() {
        return Map.of("number", InputTypes.DOUBLE);
    }

    @Override
    public Map<String, Object> exec(Map<String, Object> inputs) {
        Map<String, Object> outputs;
        double number = (double) inputs.get("number");
        outputs = Map.of("number", Math.floor(number));
        return outputs;
    }

    @Override
    public void validateInputs() {
    }
}

