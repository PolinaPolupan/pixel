package com.example.mypixel.model.node;

import com.example.mypixel.model.InputDeserializer;
import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
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
    @Setter(AccessLevel.NONE)
    Long id;
    @NonNull
    @Setter(AccessLevel.NONE)
    NodeType type;
    @JsonDeserialize(contentUsing = InputDeserializer.class)
    Map<String, Object> inputs;

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
