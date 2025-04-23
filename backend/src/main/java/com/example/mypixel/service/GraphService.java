package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidGraph;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.node.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class GraphService {

    private final NodeProcessorService nodeProcessorService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GraphService(NodeProcessorService nodeProcessorService, SimpMessagingTemplate messagingTemplate) {
        this.nodeProcessorService = nodeProcessorService;
        this.messagingTemplate = messagingTemplate;
    }

    public void processGraph(Graph graph, String sceneId) {
        log.info("Processing scene {}", sceneId);

        validateGraph(graph);

        Iterator<Node> iterator = graph.iterator();

        int totalNodes = graph.getNodes().size();
        int processedNodes = 0;

        while (iterator.hasNext()) {
            Node node = iterator.next();
            node.setSceneId(sceneId);

            nodeProcessorService.processNode(node);
            processedNodes++;

            sendProgressWebSocket(sceneId, processedNodes, totalNodes);

            log.info("Node with id: {} is processed", node.getId());
        }

        sendCompletedWebSocket(sceneId);
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

    private void sendProgressWebSocket(String sceneId, int processed, int total) {
        try {
            int percent = total > 0 ? (int) Math.round((double) processed / total * 100) : 0;

            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "in_progress");
            payload.put("processedNodes", processed);
            payload.put("totalNodes", total);
            payload.put("progressPercent", percent);
            payload.put("message", String.format("Progress: %d/%d nodes processed (%d%%)",
                    processed, total, percent));

            messagingTemplate.convertAndSend("/topic/processing/" + sceneId, payload);
            log.debug("Sent progress WebSocket: {}/{} ({}%)", processed, total, percent);
        } catch (Exception e) {
            log.error("Failed to send progress WebSocket: {}", e.getMessage());
        }
    }

    private void sendCompletedWebSocket(String sceneId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "completed");
            payload.put("message", "Completed");

            messagingTemplate.convertAndSend("/topic/processing/" + sceneId, payload);
            log.debug("Sent completed WebSocket");
        } catch (Exception e) {
            log.error("Failed to send completed WebSocket: {}", e.getMessage());
        }
    }
}
