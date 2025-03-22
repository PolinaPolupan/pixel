package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.model.NodeReference;
import com.example.mypixel.model.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@Service
@Slf4j
public class GraphService {

    private final StorageService storageService;
    private final StorageService tempStorageService;

    private final NodeProcessorService nodeProcessorService;

    // Hash map to store node IDs and their corresponding output files
    private final Map<Long, List<String>> nodeOutputFiles = new HashMap<>();
    private Map<Long, List<Long>> parentListMap = new HashMap<>();
    // Hash map to store temp files names and their corresponding input files
    private final HashMap<String, String> tempToOriginalFilenameMap = new HashMap<>();

    private final Map<NodeType, BiConsumer<Node, String>> processorMap = Map.of(
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
            nodeOutputFiles.clear();
            tempToOriginalFilenameMap.clear();
            Iterator<Node> iterator = graph.iterator(id);

            while (iterator.hasNext()) {
                Node node = iterator.next();
                processNode(node, getInputs(node));

                log.info("Node with id: {} is processed", node.getId());
            }
        }
        // Clean up all temporary files after processing is complete
        tempStorageService.deleteAll();
    }

    public void processNode(Node node, List<String> inputFiles) {
        nodeOutputFiles.computeIfAbsent(node.getId(), k -> new ArrayList<>());

        for (Object param : node.getParams().values()) {
            if (param instanceof NodeReference) {
                log.info(((NodeReference) param).getNodeId().toString());
            }
        }

        for (String file: inputFiles) {
            String tempFile;
            if (node.getType().equals(NodeType.INPUT)) tempFile = loadToTempFile(node, storageService.loadAsResource(file));
            else tempFile = loadToTempFile(node, tempStorageService.loadAsResource(file));

            NodeType type = node.getType();

            BiConsumer<Node, String> processor = processorMap.get(type);
            processor.accept(node, tempFile);
            node.getOutputs().add(tempFile);
        }
    }

    public void processInputNode(Node node, String inputFile) {
        nodeProcessorService.processInputNode(node, inputFile);
    }

    public void processGaussianBlurNode(Node node, String inputFile) {
        nodeProcessorService.processGaussianBlurNode(node, inputFile);
    }

    public void processOutputNode(Node node, String inputFile) {
        String originalFilename = tempToOriginalFilenameMap.get(inputFile);
        nodeProcessorService.processOutputNode(node, inputFile, originalFilename);
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

    private String loadToTempFile(Node node, Resource file) {
        String tempFile = tempStorageService.createTempFileFromResource(file);
        nodeOutputFiles.get(node.getId()).add(tempFile);
        if (tempToOriginalFilenameMap.get(file.getFilename()) == null) { // no ancestors
            tempToOriginalFilenameMap.put(tempFile, file.getFilename());
        } else {
            tempToOriginalFilenameMap.put(tempFile, tempToOriginalFilenameMap.get(file.getFilename()));
        }

        return tempFile;
    }

    private List<String> getInputs(Node node) {
        if (node.getType().equals(NodeType.INPUT)) return (List<String>) node.getParams().get("files");
        List<String> files = List.of();
        for (Long parentId: parentListMap.get(node.getId())) {
            files = nodeOutputFiles.get(parentId);
        }

        return files;
    }
}
