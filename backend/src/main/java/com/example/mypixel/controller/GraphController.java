package com.example.mypixel.controller;


import com.example.mypixel.model.Graph;
import com.example.mypixel.service.FileManager;
import com.example.mypixel.service.GraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/scene/{sceneId}/graph")
public class GraphController {

    private final GraphService graphService;
    private final FileManager fileManager;
    private final ObjectMapper graphObjectMapper;

    @Autowired
    public GraphController(GraphService graphService, FileManager fileManager, ObjectMapper graphObjectMapper) {
        this.graphService = graphService;
        this.fileManager = fileManager;
        this.graphObjectMapper = graphObjectMapper;
    }

    @PostMapping
    public ResponseEntity<?> processGraph(@RequestBody String graphJson,
                                          @PathVariable String sceneId) {
        if (!fileManager.sceneExists(sceneId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Scene not found: " + sceneId);
        }

        try {
            Graph graph = graphObjectMapper.readValue(graphJson, Graph.class);
            graphService.processGraph(graph, sceneId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing graph: " + e.getMessage());
        }
    }
}
