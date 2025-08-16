package com.example.pixel.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
public class Node {

    @NonNull
    private Long id;

    @NonNull
    private String type;

    @NonNull
    @JsonDeserialize(contentUsing = InputDeserializer.class)
    private Map<String, Object> inputs;

    @JsonCreator
    public Node(
            @JsonProperty("id") Long id,
            @JsonProperty("type") String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        this.id = id;
        this.type = type;
        this.inputs = inputs;
    }
}