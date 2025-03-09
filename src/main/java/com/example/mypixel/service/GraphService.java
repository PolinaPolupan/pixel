package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GraphService {

    private final StorageService storageService;

    private final FilteringService filteringService;

    @Autowired
    public GraphService(StorageService storageService, FilteringService filteringService) {
        this.storageService = storageService;
        this.filteringService = filteringService;
    }

    private final HashSet<String> nodesTypes = new HashSet<>(Arrays.asList("InputNode", "OutputNode", "GaussianBlurNode"));

    public void  processGraph(Graph graph) {
        Resource outputImage = null;
        for (Node node : graph.getNodes()) {
            if (!nodesTypes.contains(node.getType())) {
                throw new InvalidNodeType("Invalid node type: " + node.getType());
            }
            if (node.getType().equals("InputNode")) {
                outputImage = storageService.loadAsResource((String) node.getParams().get("filename"));
                log.info("InputNode processed");
            }
            if (node.getType().equals("GaussianBlurNode")) {
                filteringService.gaussianBlur(outputImage.getFilename());
                log.info("GaussianBlurNode processed");
            }
            if (node.getType().equals("OutputNode")) {
                storageService.store(outputImage, (String) node.getParams().get("filename"));
                log.info("OutputNode processed");
            }
        }
    }
}
