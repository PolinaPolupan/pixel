package com.example.mypixel.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    private final Long sceneId = 1L;

    @Test
    void sendProgress_shouldSendCorrectMessage() {
        int processed = 5;
        int total = 10;
        int expectedPercent = 50;

        notificationService.sendProgress(sceneId, processed, total);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        String destination = destinationCaptor.getValue();
        Map<String, Object> payload = payloadCaptor.getValue();

        assertEquals("/topic/processing/" + sceneId, destination);
        assertEquals(sceneId, payload.get("sceneId"));
        assertEquals("in_progress", payload.get("status"));
        assertEquals(processed, payload.get("processedNodes"));
        assertEquals(total, payload.get("totalNodes"));
        assertEquals(expectedPercent, payload.get("progressPercent"));
        assertEquals(String.format("Progress: %d/%d nodes processed (%d%%)",
                processed, total, expectedPercent), payload.get("message"));
    }

    @Test
    void sendProgress_withZeroTotal_shouldHandleZeroDivision() {
        int processed = 0;
        int total = 0;

        notificationService.sendProgress(sceneId, processed, total);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();

        assertEquals(0, payload.get("progressPercent"));
    }

    @Test
    void sendProgress_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), (Object) any());

        assertDoesNotThrow(() -> notificationService.sendProgress(sceneId, 5, 10));
    }

    @Test
    void sendCompleted_shouldSendCorrectMessage() {
        notificationService.sendCompleted(sceneId);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        String destination = destinationCaptor.getValue();
        Map<String, Object> payload = payloadCaptor.getValue();

        assertEquals("/topic/processing/" + sceneId, destination);
        assertEquals(sceneId, payload.get("sceneId"));
        assertEquals("completed", payload.get("status"));
        assertEquals("Completed", payload.get("message"));
    }

    @Test
    void sendCompleted_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), (Object) any());

        assertDoesNotThrow(() -> notificationService.sendCompleted(sceneId));
    }

    @Test
    void sendError_shouldSendCorrectMessage() {
        String errorMessage = "Test error message";

        notificationService.sendError(sceneId, errorMessage);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        String destination = destinationCaptor.getValue();
        Map<String, Object> payload = payloadCaptor.getValue();

        assertEquals("/topic/processing/" + sceneId, destination);
        assertEquals(sceneId, payload.get("sceneId"));
        assertEquals("failed", payload.get("status"));
        assertEquals("Failed: " + errorMessage, payload.get("message"));
    }

    @Test
    void sendError_withNullErrorMessage_shouldHandleNullValue() {
        notificationService.sendError(sceneId, null);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("Failed: null", payload.get("message"));
    }

    @Test
    void sendError_whenMessageTemplateThrowsException_shouldNotPropagateException() {
        doThrow(new RuntimeException("Test exception")).when(messagingTemplate)
                .convertAndSend(anyString(), (Object) any());

        assertDoesNotThrow(() -> notificationService.sendError(sceneId, "Error"));
    }
}