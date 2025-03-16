package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GraphService {

    private final StorageService storageService;
    private final StorageService tempStorageService;

    private final NodeProcessorService nodeProcessorService;

    // Hash map to store node IDs and their corresponding output files
    private Map<Long, String> nodeOutputFiles = new HashMap<>();
    private Map<Long, List<Long>> parentListMap = new HashMap<>();

    @Autowired
    public GraphService(
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService,
            NodeProcessorService nodeProcessorService) {
        this.storageService = storageService;
        this.tempStorageService = tempStorageService;
        this.nodeProcessorService = nodeProcessorService;
    }

    public void processGraph(Graph graph) {
        validate(graph);
        parentListMap = graph.buildParentListMap();

        // Initialize temporary storage for processing artifacts
        tempStorageService.deleteAll();
        tempStorageService.init();

        List<Long> startingNodeIds = new ArrayList<>();

        // Identify all input nodes to use as starting points for graph traversal
        for (Node node: graph.getNodes()) {
            if (node.getType().equals("InputNode")) startingNodeIds.add(node.getId());
        }

        // Process each subgraph starting from each input node
        for (Long id: startingNodeIds) {
            nodeOutputFiles = new HashMap<>();
            Iterator<Node> iterator = graph.iterator(id);

            while (iterator.hasNext()) {
                Node node = iterator.next();
                String nodeType = node.getType();

                switch (nodeType) {
                    case "InputNode" -> processInputNode(node);
                    case "GaussianBlurNode" -> processGaussianBlurNode(node);
                    case "OutputNode" -> processOutputNode(node);
                    default -> throw new InvalidNodeType("Invalid node type: " + nodeType);
                }

                log.info("Node with id: " + node.getId() + " is processed");
            }
        }
        // Clean up all temporary files after processing is complete
        tempStorageService.deleteAll();
    }

    public void processInputNode(Node node) {
        String tempFile = nodeProcessorService.processInputNode(node);
        nodeOutputFiles.put(node.getId(), tempFile);
    }

    public void processGaussianBlurNode(Node node) {
        for (Long parentId: parentListMap.get(node.getId())) {
            if (nodeOutputFiles.containsKey(parentId)) {
                String tempFile = nodeProcessorService.processGaussianBlurNode(node, nodeOutputFiles.get(parentId));
                nodeOutputFiles.put(node.getId(), tempFile);
            }
        }
    }

    public void processOutputNode(Node node) {
        for (Long parentId: parentListMap.get(node.getId())) {
            if (nodeOutputFiles.containsKey(parentId)) {
                String tempFile = nodeProcessorService.processOutputNode(node, nodeOutputFiles.get(parentId));
                nodeOutputFiles.put(node.getId(), tempFile);
            }
        }
    }

    private void validate(Graph graph) {
        // Check for supported node types
        Set<String> supportedTypes = Set.of("InputNode", "GaussianBlurNode", "OutputNode");
        for (Node node: graph.getNodes()) {
            if (!supportedTypes.contains(node.getType())) {
                throw new InvalidNodeType("Invalid node type: " + node.getType());
            }
        }
    }
}
