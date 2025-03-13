package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GraphService {

    private final StorageService storageService;
    private final StorageService tempStorageService;

    private final NodeProcessorService nodeProcessorService;

    private Queue<String> target = new LinkedList<>();

    @Autowired
    public GraphService(
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService,
            NodeProcessorService nodeProcessorService) {
        this.storageService = storageService;
        this.tempStorageService = tempStorageService;
        this.nodeProcessorService = nodeProcessorService;
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
        String tempFile = nodeProcessorService.processInputNode(node);
        target.add(tempFile);
    }

    public void processGaussianBlurNode(Node node) {
        String tempName = nodeProcessorService.processGaussianBlurNode(node, target.poll());
        target.add(tempName);
    }

    public void processOutputNode(Node node) {
        String tempFile = nodeProcessorService.processOutputNode(node, target.poll());
        target.add(tempFile);
    }
}
