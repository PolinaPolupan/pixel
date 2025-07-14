package com.example.mypixel.service;

import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final String processingTopic = "/topic/processing/";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendTaskStatus(GraphExecutionTask task) {
        try {
            Long taskId = task.getId();
            String destination = processingTopic + taskId;
            TaskStatus status = task.getStatus();

            messagingTemplate.convertAndSend(destination, task);

            log.debug("WebSocket notification sent | Task ID: {} | Status: {} | Destination: {} | Nodes: {}/{}",
                    taskId,
                    status,
                    destination,
                    task.getProcessedNodes(),
                    task.getTotalNodes());

        } catch (Exception e) {
            log.error("WebSocket notification failed | Task ID: {} | Status: {} | Error: {} | Stack: {}",
                    task.getId(),
                    task.getStatus(),
                    e.getMessage(),
                    e.getClass().getSimpleName(),
                    e);
        }
    }
}
