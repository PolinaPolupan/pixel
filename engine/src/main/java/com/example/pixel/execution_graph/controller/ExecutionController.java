package com.example.pixel.execution_graph.controller;

import com.example.pixel.execution_graph.dto.CreateExecutionGraphRequest;
import com.example.pixel.execution_graph.dto.ExecutionGraphPayload;
import com.example.pixel.execution_graph.service.GraphService;
import com.example.pixel.execution_task.dto.ExecutionTaskPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/graph")
public class ExecutionController {

    private final GraphService graphService;

    @PostMapping("/")
    public ResponseEntity<ExecutionGraphPayload> create(@RequestBody CreateExecutionGraphRequest createExecutionGraphRequest) {
        ExecutionGraphPayload graph = graphService.createExecutionGraph(createExecutionGraphRequest);
        graphService.updateLastAccessed(graph.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(graph);
    }

    @PostMapping("/{id}/exec")
    public ResponseEntity<ExecutionTaskPayload> execute(@PathVariable Long id) {
        graphService.updateLastAccessed(id);
        ExecutionTaskPayload task = graphService.executeGraph(id);
        return ResponseEntity.ok(task);
    }
}
