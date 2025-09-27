package com.example.pixel.common;

import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
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
    private ArgumentCaptor<GraphExecutionPayload> taskCaptor;

    private final Long sceneId = 1L;
    private final Long taskId = 1L;
    private GraphExecutionEntity graphExecutionEntity;

    @BeforeEach
    void setUp() {
        graphExecutionEntity = new GraphExecutionEntity();
        graphExecutionEntity.setId(taskId);
        graphExecutionEntity.setId(sceneId);
    }

    @Test
    void sendProgress_shouldSendCorrectMessage() {
        int processed = 5;
        int total = 10;

        graphExecutionEntity.setStatus(GraphExecutionStatus.RUNNING);
        graphExecutionEntity.setProcessedNodes(processed);
        graphExecutionEntity.setTotalNodes(total);

        notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        GraphExecutionPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getId());
        assertEquals(GraphExecutionStatus.RUNNING, sentTask.getStatus());
        assertEquals(processed, sentTask.getProcessedNodes());
        assertEquals(total, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_withZeroTotal_shouldHandleZeroDivision() {
        graphExecutionEntity.setStatus(GraphExecutionStatus.RUNNING);
        graphExecutionEntity.setProcessedNodes(0);
        graphExecutionEntity.setTotalNodes(0);

        notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        GraphExecutionPayload sentTask = taskCaptor.getValue();
        assertEquals(0, sentTask.getProcessedNodes());
        assertEquals(0, sentTask.getTotalNodes());
    }

    @Test
    void sendProgress_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        graphExecutionEntity.setStatus(GraphExecutionStatus.RUNNING);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(GraphExecutionEntity.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity)));
    }

    @Test
    void sendCompleted_shouldSendCorrectMessage() {
        graphExecutionEntity.setStatus(GraphExecutionStatus.COMPLETED);

        notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        GraphExecutionPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getId());
        assertEquals(GraphExecutionStatus.COMPLETED, sentTask.getStatus());
    }

    @Test
    void sendCompleted_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        graphExecutionEntity.setStatus(GraphExecutionStatus.COMPLETED);
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(GraphExecutionPayload.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity)));
    }

    @Test
    void sendError_shouldSendCorrectMessage() {
        String errorMessage = "Test error message";
        graphExecutionEntity.setStatus(GraphExecutionStatus.FAILED);
        graphExecutionEntity.setErrorMessage(errorMessage);

        notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        String destination = destinationCaptor.getValue();
        GraphExecutionPayload sentTask = taskCaptor.getValue();

        assertEquals("/topic/processing/" + taskId, destination);
        assertEquals(sceneId, sentTask.getId());
        assertEquals(GraphExecutionStatus.FAILED, sentTask.getStatus());
        assertEquals(errorMessage, sentTask.getErrorMessage());
    }

    @Test
    void sendError_withNullErrorMessage_shouldHandleNullValue() {
        graphExecutionEntity.setStatus(GraphExecutionStatus.FAILED);
        graphExecutionEntity.setErrorMessage(null);

        notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), taskCaptor.capture());

        GraphExecutionPayload sentTask = taskCaptor.getValue();
        assertNull(sentTask.getErrorMessage());
    }

    @Test
    void sendError_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        graphExecutionEntity.setStatus(GraphExecutionStatus.FAILED);
        graphExecutionEntity.setErrorMessage("Error");
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), any(GraphExecutionPayload.class));

        assertDoesNotThrow(() -> notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity)));
    }

    @Test
    void sendTaskStatus_withNullId_shouldUseNullInDestination() {
        graphExecutionEntity.setId(null);
        graphExecutionEntity.setStatus(GraphExecutionStatus.RUNNING);

        notificationService.sendTaskStatus(GraphExecutionPayload.fromEntity(graphExecutionEntity));

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), any(GraphExecutionPayload.class));

        String destination = destinationCaptor.getValue();
        assertEquals("/topic/processing/null", destination);
    }
}