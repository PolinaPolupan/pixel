package com.example.pixel.common;

import com.example.pixel.common.service.NotificationService;
import com.example.pixel.execution_task.entity.ExecutionTaskEntity;
import com.example.pixel.execution_task.dto.ExecutionTaskPayload;
import com.example.pixel.execution_task.dto.ExecutionTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<ExecutionTaskPayload> taskCaptor;

    private final Long sceneId = 1L;
    private final Long taskId = 1L;
    private ExecutionTaskEntity executionTaskEntity;

    @BeforeEach
    void setUp() {
        executionTaskEntity = new ExecutionTaskEntity();
        executionTaskEntity.setId(taskId);
        executionTaskEntity.setId(sceneId);
    }

    @Test
    void sendProgress_shouldSendCorrectMessage() {
        int processed = 5;
        int total = 10;

        executionTaskEntity.setStatus(ExecutionTaskStatus.RUNNING);
        executionTaskEntity.setProcessedNodes(processed);
        executionTaskEntity.setTotalNodes(total);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        ExecutionTaskPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getId());
        assertEquals(ExecutionTaskStatus.RUNNING, sentTask.getStatus());
        assertEquals(processed, sentTask.getProcessedNodes());
        assertEquals(total, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_withZeroTotal_shouldHandleZeroDivision() {
        executionTaskEntity.setStatus(ExecutionTaskStatus.RUNNING);
        executionTaskEntity.setProcessedNodes(0);
        executionTaskEntity.setTotalNodes(0);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        ExecutionTaskPayload sentTask = taskCaptor.getValue();
        assertEquals(0, sentTask.getProcessedNodes());
        assertEquals(0, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        executionTaskEntity.setStatus(ExecutionTaskStatus.RUNNING);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ExecutionTaskEntity.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity)));
    }

    @Test
    void sendCompleted_shouldSendCorrectMessage() {
        executionTaskEntity.setStatus(ExecutionTaskStatus.COMPLETED);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        ExecutionTaskPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getId());
        assertEquals(ExecutionTaskStatus.COMPLETED, sentTask.getStatus());
    }

    @Test
    void sendCompleted_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        executionTaskEntity.setStatus(ExecutionTaskStatus.COMPLETED);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ExecutionTaskPayload.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity)));
    }

    @Test
    void sendError_shouldSendCorrectMessage() {
        String errorMessage = "Test error message";
        executionTaskEntity.setStatus(ExecutionTaskStatus.FAILED);
        executionTaskEntity.setErrorMessage(errorMessage);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        ExecutionTaskPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getId());
        assertEquals(ExecutionTaskStatus.FAILED, sentTask.getStatus());
        assertEquals(errorMessage, sentTask.getErrorMessage());
    }

    @Test
    void sendError_withNullErrorMessage_shouldHandleNullValue() {
        executionTaskEntity.setStatus(ExecutionTaskStatus.FAILED);
        executionTaskEntity.setErrorMessage(null);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        ExecutionTaskPayload sentTask = taskCaptor.getValue();
        assertNull(sentTask.getErrorMessage());
    }

    @Test
    void sendError_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        executionTaskEntity.setStatus(ExecutionTaskStatus.FAILED);
        executionTaskEntity.setErrorMessage("Error");
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ExecutionTaskPayload.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity)));
    }

    @Test
    void sendTaskStatus_withNullId_shouldUseNullInDestination() {
        executionTaskEntity.setId(null);
        executionTaskEntity.setStatus(ExecutionTaskStatus.RUNNING);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), any(ExecutionTaskPayload.class));

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/processing/null", destination);
    }
}