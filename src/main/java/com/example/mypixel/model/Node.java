package com.example.mypixel.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@Data
public class Node {
    @NonNull
    Long id;
    @NonNull
    NodeType type;
    @JsonDeserialize(contentUsing = InputDeserializer.class)
    Map<String, Object> inputs;

    public Node(@NonNull Long id, @NonNull NodeType type, Map<String, Object> inputs) {
        this.id = id;
        this.type = type;
        this.inputs = inputs;
    }

    public void validateInputs() {}

    public void exec(Map<String, Object> inputs) {}
}
