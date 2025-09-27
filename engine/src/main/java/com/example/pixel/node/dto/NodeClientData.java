package com.example.pixel.node.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class NodeClientData {
    private Metadata meta;
    private Map<String, Object> inputs;
}
