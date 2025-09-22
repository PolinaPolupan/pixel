package com.example.pixel.common.service;

import com.example.pixel.execution_task.model.ExecutionTaskPayload;
import com.example.pixel.execution_task.model.ExecutionTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final String processingTopic = "/topic/processing/";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendTaskStatus(ExecutionTaskPayload task) {
        try {
            String destination = processingTopic + task.getId();
            ExecutionTaskStatus status = task.getStatus();

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
