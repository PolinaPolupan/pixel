package com.example.mypixel.model.node;

import com.example.mypixel.model.InputDeserializer;
import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
public class Node {

    @NonNull
    @Setter(AccessLevel.NONE)
    Long id;

    @NonNull
    @Setter(AccessLevel.NONE)
    NodeType type;

    @JsonDeserialize(contentUsing = InputDeserializer.class)
    Map<String, Object> inputs;

    @JsonCreator
    public Node(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull NodeType type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        this.id = id;
        this.type = type;
        this.inputs = inputs;
    }

    public Map<String, ParameterType> getInputTypes() {
        return null;
    }

    public Map<String, ParameterType> getOutputTypes() {
        return null;
    }

    public Map<String, Object> exec() {
        return null;
    }
}
