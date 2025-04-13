package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidGraph;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.node.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class GraphService {

    private final NodeProcessorService nodeProcessorService;

    @Autowired
    public GraphService(NodeProcessorService nodeProcessorService) {
        this.nodeProcessorService = nodeProcessorService;
    }

    public void processGraph(Graph graph, String sceneId) {
        // Initialize temporary storage for processing artifacts
        log.info("Processing scene {}", sceneId);

        validateGraph(graph);

        Iterator<Node> iterator = graph.iterator();

        while (iterator.hasNext()) {
            Node node = iterator.next();

            Map<String, Object> mutableInputs = new HashMap<>(node.getInputs() != null ?
                    node.getInputs() :
                    new HashMap<>());
            mutableInputs.put("sceneId", sceneId);
            node.setInputs(mutableInputs);

            nodeProcessorService.processNode(node);

            log.info("Node with id: {} is processed", node.getId());
        }
    }

    public void validateGraph(Graph graph) {
        Set<Long> seenIds = new HashSet<>();
        List<Long> duplicateIds = new ArrayList<>();

        for (Node node: graph.getNodes()) {
            if (!seenIds.add(node.getId())) {
                // If we couldn't add to the set, it's a duplicate
                duplicateIds.add(node.getId());
            }
        }

        if (!duplicateIds.isEmpty()) {
            throw new InvalidGraph("Graph contains nodes with duplicate IDs: " + duplicateIds);
        }

        log.info("Graph validation passed: no duplicate node IDs found");
    }
}
