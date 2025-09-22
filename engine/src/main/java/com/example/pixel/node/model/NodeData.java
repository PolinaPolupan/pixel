package com.example.pixel.node.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class NodeData {
    private Metadata meta;
    private Map<String, Object> inputs;
}
