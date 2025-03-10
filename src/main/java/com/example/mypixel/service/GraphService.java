package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
                Map<String, Object> params = node.getParams();
                int sizeX = (int) params.getOrDefault("sizeX", 0);
                int sizeY = (int) params.getOrDefault("sizeY", 0);
                double sigmaX = (double) params.getOrDefault("sigmaX", 0.0);
                double sigmaY = (double) params.getOrDefault("sigmaY", 0.0);

                filteringService.gaussianBlur(outputImage.getFilename(), sizeX, sizeY, sigmaX, sigmaY);
                log.info("GaussianBlurNode processed");
            }
            if (node.getType().equals("OutputNode")) {
                storageService.store(outputImage, (String) node.getParams().get("filename"));
                log.info("OutputNode processed");
            }
        }
    }
}
