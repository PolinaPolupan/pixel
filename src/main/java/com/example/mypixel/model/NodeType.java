package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidNodeType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum NodeType {
    INPUT("Input"),
    GAUSSIAN_BLUR("GaussianBlur"),
    OUTPUT("Output"),
    UNKNOWN("Unknown");

    private final String jsonValue;

    NodeType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static NodeType fromString(String value) {
        for (NodeType nodeType : NodeType.values()) {
            if (nodeType.getJsonValue().equalsIgnoreCase(value)) {
                return nodeType;
            }
        }
        throw new InvalidNodeType("Invalid node type: " + value);
    }
}