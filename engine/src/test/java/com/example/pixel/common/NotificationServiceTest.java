package com.example.pixel.common;

import com.example.pixel.execution_task.ExecutionTask;
import com.example.pixel.execution_task.ExecutionTaskPayload;
import com.example.pixel.execution_task.ExecutionTaskStatus;
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
    private ExecutionTask executionTask;

    @BeforeEach
    void setUp() {
        executionTask = new ExecutionTask();
        executionTask.setId(taskId);
        executionTask.setSceneId(sceneId);
    }

    @Test
    void sendProgress_shouldSendCorrectMessage() {
        int processed = 5;
        int total = 10;

        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
        executionTask.setProcessedNodes(processed);
        executionTask.setTotalNodes(total);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        ExecutionTaskPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getSceneId());
        assertEquals(ExecutionTaskStatus.RUNNING, sentTask.getStatus());
        assertEquals(processed, sentTask.getProcessedNodes());
        assertEquals(total, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_withZeroTotal_shouldHandleZeroDivision() {
        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
        executionTask.setProcessedNodes(0);
        executionTask.setTotalNodes(0);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        ExecutionTaskPayload sentTask = taskCaptor.getValue();
        assertEquals(0, sentTask.getProcessedNodes());
        assertEquals(0, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ExecutionTask.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask)));
    }

    @Test
    void sendCompleted_shouldSendCorrectMessage() {
        executionTask.setStatus(ExecutionTaskStatus.COMPLETED);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        ExecutionTaskPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getSceneId());
        assertEquals(ExecutionTaskStatus.COMPLETED, sentTask.getStatus());
    }

    @Test
    void sendCompleted_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        executionTask.setStatus(ExecutionTaskStatus.COMPLETED);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ExecutionTaskPayload.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask)));
    }

    @Test
    void sendError_shouldSendCorrectMessage() {
        String errorMessage = "Test error message";
        executionTask.setStatus(ExecutionTaskStatus.FAILED);
        executionTask.setErrorMessage(errorMessage);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        ExecutionTaskPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getSceneId());
        assertEquals(ExecutionTaskStatus.FAILED, sentTask.getStatus());
        assertEquals(errorMessage, sentTask.getErrorMessage());
    }

    @Test
    void sendError_withNullErrorMessage_shouldHandleNullValue() {
        executionTask.setStatus(ExecutionTaskStatus.FAILED);
        executionTask.setErrorMessage(null);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        ExecutionTaskPayload sentTask = taskCaptor.getValue();
        assertNull(sentTask.getErrorMessage());
    }

    @Test
    void sendError_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        executionTask.setStatus(ExecutionTaskStatus.FAILED);
        executionTask.setErrorMessage("Error");
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ExecutionTaskPayload.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask)));
    }

    @Test
    void sendTaskStatus_withNullId_shouldUseNullInDestination() {
        executionTask.setId(null);
        executionTask.setStatus(ExecutionTaskStatus.RUNNING);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTask));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), any(ExecutionTaskPayload.class));

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/processing/null", destination);
    }
}