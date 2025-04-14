package com.example.mypixel.controller;


import com.example.mypixel.model.Graph;
import com.example.mypixel.service.GraphService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void processGraph(@RequestBody String graphJson,
                             @PathVariable String sceneId) throws JsonProcessingException {
        Graph graph = graphObjectMapper.readValue(graphJson, Graph.class);
        graphService.processGraph(graph, sceneId);
    }
}
