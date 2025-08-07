package com.example.mypixel.node;

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
}