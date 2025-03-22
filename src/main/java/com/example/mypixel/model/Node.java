package com.example.mypixel.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Node {
    @NonNull
    Long id;
    @NonNull
    NodeType type;
    @JsonDeserialize(contentUsing = ParameterDeserializer.class)
    Map<String, Object> params;
    List<Object> outputs;

    public Node(@NonNull Long id, @NonNull NodeType type, Map<String, Object> params) {
        this.id = id;
        this.type = type;
        this.params = params;
        outputs = new ArrayList<>();
    }
}
