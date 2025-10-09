package com.example.pixel.graph.controller;

import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.service.GraphService;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(graphService.create(createGraphRequest));
    }

    @PostMapping("/{id}")
    public ResponseEntity<GraphExecutionPayload> execute(@PathVariable String id) {
        GraphPayload graphPayload = graphService.findById(id);
        return ResponseEntity.ok(graphService.execute(graphPayload));
    }
}
