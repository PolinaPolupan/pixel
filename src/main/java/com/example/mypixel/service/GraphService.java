package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GraphService {

    private final StorageService tempStorageService;
    private final StorageService storageService;

    private final FilteringService filteringService;

    private Queue<String> target = new LinkedList<>();

    @Autowired
    public GraphService(
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService,
                        FilteringService filteringService) {
        this.tempStorageService = tempStorageService;
        this.storageService = storageService;
        this.filteringService = filteringService;
    }

    public void  processGraph(Graph graph) {
        // Clean up temp images
        tempStorageService.deleteAll();
        tempStorageService.init();

        target = new LinkedList<>();

        for (Node node : graph.getNodes()) {
            String nodeType = node.getType();

            switch (nodeType) {
                case "InputNode" -> processInputNode(node);
                case "GaussianBlurNode" -> processGaussianBlurNode(node);
                case "OutputNode" -> processOutputNode(node);
                default -> throw new InvalidNodeType("Invalid node type: " + nodeType);
            }
        }
    }

    public void processInputNode(Node node) {
        String filename = (String) node.getParams().get("filename");
        String tempFile = tempStorageService.createTempFileFromResource(storageService.loadAsResource(filename));
        target.add(tempFile);
        log.info("InputNode processed");
    }

    public void processGaussianBlurNode(Node node) {
        Map<String, Object> params = node.getParams();
        int sizeX = (int) params.getOrDefault("sizeX", 1);
        int sizeY = (int) params.getOrDefault("sizeY", 1);
        double sigmaX = (double) params.getOrDefault("sigmaX", 0.0);
        double sigmaY = (double) params.getOrDefault("sigmaY", 0.0);

        String tempName = tempStorageService.createTempFileFromFilename(target.poll());
        if (tempName != null) {
            filteringService.gaussianBlur(tempName, sizeX, sizeY, sigmaX, sigmaY);
            target.add(tempName);
        }

        log.info("GaussianBlurNode processed");
    }

    public void processOutputNode(Node node) {
        String filename = target.poll();
        Resource outputImage = tempStorageService.loadAsResource(filename);

        storageService.store(outputImage, (String) node.getParams().get("filename"));
        String tempFile = tempStorageService.createTempFileFromResource(outputImage);
        target.add(tempFile);

        log.info("OutputNode processed");
    }
}
