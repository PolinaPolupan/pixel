package com.example.pixel.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class NodeData {
    private Metadata meta;
    private Map<String, Object> inputs;
}
