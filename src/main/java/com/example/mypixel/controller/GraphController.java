package com.example.mypixel.controller;


import com.example.mypixel.model.Graph;
import com.example.mypixel.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/graph")
public class GraphController {

    private final GraphService graphService;

    @Autowired
    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @PostMapping
    public void processGraph(@RequestBody Graph graph) {
        graphService.processGraph(graph);
    }
}
