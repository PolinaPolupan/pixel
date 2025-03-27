package com.example.mypixel.model.node;

import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.ParameterType;

import java.util.Map;

public class FloorNode extends Node {

    public FloorNode(
            Long id,
            NodeType type,
            Map<String, Object> inputs
    ) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of("number", ParameterType.DOUBLE);
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("number", ParameterType.DOUBLE);
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;
        double number = (double) inputs.get("number");
        outputs = Map.of("number", Math.floor(number));
        return outputs;
    }
}

