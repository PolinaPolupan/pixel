package com.example.pixel.node;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class NodeData {
    private Metadata meta;
    private Map<String, Object> data;
}
