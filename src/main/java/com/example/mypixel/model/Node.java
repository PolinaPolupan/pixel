package com.example.mypixel.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Map;

@Getter
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InputNode.class, name = "Input"),
        @JsonSubTypes.Type(value = GaussianBlurNode.class, name = "GaussianBlur"),
        @JsonSubTypes.Type(value = OutputNode.class, name = "Output"),
        @JsonSubTypes.Type(value = FloorNode.class, name = "Floor")
})
public class Node {
    @NonNull
    Long id;
    @NonNull
    NodeType type;
    @JsonDeserialize(contentUsing = InputDeserializer.class)
    Map<String, Object> inputs;

    public Map<String, InputTypes> getInputTypes() {
        return null;
    };

    public void validateInputs() {}

    public Map<String, Object> exec(Map<String, Object> inputs) {
        return null;
    }
}
