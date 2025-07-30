package com.example.mypixel.controller;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.TaskPayload;
import com.example.mypixel.service.GraphService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/scene/{sceneId}/graph")
public class GraphController {

    private final GraphService graphService;
    private final ObjectMapper graphObjectMapper;

    @PostMapping
    public ResponseEntity<TaskPayload> executeGraph(
            @PathVariable Long sceneId,
            @RequestBody String graphJson) throws JsonProcessingException {
        Graph graph = graphObjectMapper.readValue(graphJson, Graph.class);
        TaskPayload task = graphService.startGraphExecution(graph, sceneId);
        return ResponseEntity.ok(task);
    }
}
