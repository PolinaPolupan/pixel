package com.example.pixel.graph.controller;

import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/graph")
public class GraphController {

    private final GraphService graphService;

    @PostMapping
    public ResponseEntity<GraphPayload> create(@RequestBody CreateGraphRequest createGraphRequest) {
        GraphPayload graph = graphService.create(createGraphRequest);
        graphService.updateLastAccessed(graph.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(graph);
    }
}
