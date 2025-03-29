package com.example.mypixel.controller;


import com.example.mypixel.model.Graph;
import com.example.mypixel.service.GraphService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/graph")
public class GraphController {

    private final GraphService graphService;

    private final ObjectMapper graphObjectMapper;

    @Autowired
    public GraphController(GraphService graphService, ObjectMapper graphObjectMapper) {
        this.graphService = graphService;
        this.graphObjectMapper = graphObjectMapper;
    }

    @PostMapping
    public void processGraph(@RequestBody String graphJson) throws JsonProcessingException {
        Graph graph = graphObjectMapper.readValue(graphJson, Graph.class);
        graphService.processGraph(graph);
    }
}
