package com.example.mypixel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InputNode.class, name = "Input"),
        @JsonSubTypes.Type(value = GaussianBlurNode.class, name = "GaussianBlur"),
        @JsonSubTypes.Type(value = OutputNode.class, name = "Output")
})
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

    public Map<String, Object> exec(Map<String, Object> inputs) {
        return null;
    }
}
