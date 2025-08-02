package com.example.mypixel.controller;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.TaskPayload;
import com.example.mypixel.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/scene/{sceneId}/graph")
public class GraphController {

    private final GraphService graphService;

    @PostMapping
    public ResponseEntity<TaskPayload> executeGraph(@PathVariable Long sceneId, @RequestBody Graph graph) {
        TaskPayload task = graphService.startGraphExecution(graph, sceneId);
        return ResponseEntity.ok(task);
    }
}
