package com.example.pixel.common.service;

import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    @Value("${processing.topic}")
    private String processingTopic;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendTaskStatus(GraphExecutionPayload task) {
        try {
            String destination = processingTopic + task.getId();
            GraphExecutionStatus status = task.getStatus();

            messagingTemplate.convertAndSend(destination, task);

            log.debug("WebSocket notification sent | Task ID: {} | Status: {} | Destination: {} | Nodes: {}/{}",
                    task.getId(),
                    status,
                    destination,
                    task.getProcessedNodes(),
                    task.getTotalNodes());

        } catch (MessagingException e) {
            log.error("WebSocket notification failed | Task ID: {} | Status: {} | Error: {} | Stack: {}",
                    task.getId(),
                    task.getStatus(),
                    e.getMessage(),
                    e.getClass().getSimpleName(),
                    e);
        }
    }
}
