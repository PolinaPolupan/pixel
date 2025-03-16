package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.model.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;


@Service
@Slf4j
public class GraphService {

    private final StorageService storageService;
    private final StorageService tempStorageService;

    private final NodeProcessorService nodeProcessorService;

    // Hash map to store node IDs and their corresponding output files
    private Map<Long, String> nodeOutputFiles = new HashMap<>();
    private Map<Long, List<Long>> parentListMap = new HashMap<>();

    private final Map<NodeType, Consumer<Node>> processorMap = Map.of(
            NodeType.INPUT, this::processInputNode,
            NodeType.GAUSSIAN_BLUR, this::processGaussianBlurNode,
            NodeType.OUTPUT, this::processOutputNode
    );

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
            if (node.getType().equals(NodeType.INPUT)) startingNodeIds.add(node.getId());
        }

        // Process each subgraph starting from each input node
        for (Long id: startingNodeIds) {
            nodeOutputFiles = new HashMap<>();
            Iterator<Node> iterator = graph.iterator(id);

            while (iterator.hasNext()) {
                Node node = iterator.next();
                NodeType type = node.getType();

                Consumer<Node> processor = processorMap.get(type);
                processor.accept(node);

                log.info("Node with id: {} is processed", node.getId());
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
            if (nodeOutputFiles.get(parentId) != null) {
                String tempFile = nodeProcessorService.processGaussianBlurNode(node, nodeOutputFiles.get(parentId));
                nodeOutputFiles.put(node.getId(), tempFile);
            }
        }
    }

    public void processOutputNode(Node node) {
        for (Long parentId: parentListMap.get(node.getId())) {
            if (nodeOutputFiles.get(parentId) != null) {
                String tempFile = nodeProcessorService.processOutputNode(node, nodeOutputFiles.get(parentId));
                nodeOutputFiles.put(node.getId(), tempFile);
            }
        }
    }

    private void validate(Graph graph) {
        // Check for supported node types
        Set<NodeType> supportedTypes = Set.of(NodeType.INPUT, NodeType.GAUSSIAN_BLUR, NodeType.OUTPUT);
        for (Node node: graph.getNodes()) {
            if (!supportedTypes.contains(node.getType())) {
                throw new InvalidNodeType("Invalid node type: " + node.getType());
            }
        }
    }
}
