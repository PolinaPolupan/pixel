package com.example.mypixel.controller;


import com.example.mypixel.model.Graph;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.service.GraphService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/scene/{sceneId}/graph")
public class GraphController {

    private final GraphService graphService;

    private final ObjectMapper graphObjectMapper;

    @Autowired
    public GraphController(GraphService graphService, ObjectMapper graphObjectMapper) {
        this.graphService = graphService;
        this.graphObjectMapper = graphObjectMapper;
    }

    @PostMapping
    public ResponseEntity<GraphExecutionTask> executeGraph(
            @PathVariable String sceneId,
            @RequestBody String graphJson) throws JsonProcessingException {
        Graph graph = graphObjectMapper.readValue(graphJson, Graph.class);
        GraphExecutionTask task = graphService.startGraphExecution(graph, sceneId);
        return ResponseEntity.ok(task);
    }
}
