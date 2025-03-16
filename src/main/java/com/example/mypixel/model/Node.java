package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Node {
    @NonNull
    Long id;
    @NonNull
    NodeType type;
    Map<String, Object> params;
    List<Long> outputs;
}
