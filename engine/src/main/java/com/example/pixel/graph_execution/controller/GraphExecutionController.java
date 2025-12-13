package com.example.pixel.graph_execution.controller;

import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/graph_execution")
public class GraphExecutionController {

    private final GraphExecutionService graphExecutionService;

    @GetMapping("/{id}")
    public ResponseEntity<GraphExecutionPayload> get(@PathVariable Long id) {
        return ResponseEntity.ok(graphExecutionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<GraphExecutionPayload>> getAll(@RequestParam(required = false) String graphId) {
        if (graphId != null) {
            return ResponseEntity.ok(graphExecutionService.findByGraphId(graphId));
        }
        return ResponseEntity.ok(graphExecutionService.findAll());
    }
}
