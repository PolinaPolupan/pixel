package com.example.mypixel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final String processingTopic = "/topic/processing/";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendProgress(Long sceneId, int processed, int total) {
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

            messagingTemplate.convertAndSend(processingTopic + sceneId, payload);
            log.debug("Sent progress WebSocket: {}/{} ({}%)", processed, total, percent);
        } catch (Exception e) {
            log.error("Failed to send progress WebSocket: {}", e.getMessage());
        }
    }

    public void sendCompleted(Long sceneId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "completed");
            payload.put("message", "Completed");

            messagingTemplate.convertAndSend(processingTopic + sceneId, payload);
            log.debug("Sent completed WebSocket");
        } catch (Exception e) {
            log.error("Failed to send completed WebSocket: {}", e.getMessage());
        }
    }

    public void sendError(Long sceneId, String errorMessage) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sceneId", sceneId);
            payload.put("status", "failed");
            payload.put("message", "Failed: " + errorMessage);

            messagingTemplate.convertAndSend(processingTopic + sceneId, payload);
            log.debug("Sent error WebSocket");
        } catch (Exception e) {
            log.error("Failed to send error WebSocket: {}", e.getMessage());
        }
    }
}
