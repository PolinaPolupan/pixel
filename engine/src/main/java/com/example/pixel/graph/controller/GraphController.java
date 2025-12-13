package com.example.pixel.graph.controller;

import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph.service.GraphService;
import com.example.pixel.graph_execution.dto.GraphExecutionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/graph")
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/{id}")
    public ResponseEntity<GraphDto> get(@PathVariable String id) {
        return ResponseEntity.ok(graphService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<GraphDto>> getAll() {
        return ResponseEntity.ok(graphService.findAll());
    }

    @PostMapping
    public ResponseEntity<GraphDto> create(@RequestBody CreateGraphRequest createGraphRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(graphService.create(createGraphRequest));
    }

    @PostMapping("/{id}")
    public ResponseEntity<GraphExecutionDto> execute(@PathVariable String id) {
        GraphDto graphDto = graphService.findById(id);
        return ResponseEntity.ok(graphService.execute(graphDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        graphService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
