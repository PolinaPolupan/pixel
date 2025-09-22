package com.example.pixel.common;

import com.example.pixel.common.service.NotificationService;
import com.example.pixel.config.TestCacheConfig;
import com.example.pixel.execution_task.model.ExecutionTaskEntity;
import com.example.pixel.execution_task.model.ExecutionTaskPayload;
import com.example.pixel.execution_task.model.ExecutionTaskStatus;
import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({TestCacheConfig.class})
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class NotificationServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationService notificationService;

    private StompSession stompSession;
    private final Long taskId = 1L;
    private final Long sceneId = 1L;
    private final String processingTopic = "/topic/processing/" + taskId;
    private ExecutionTaskEntity executionTaskEntity;

    @BeforeEach
    void setupConnection() throws ExecutionException, InterruptedException, TimeoutException {
        executionTaskEntity = new ExecutionTaskEntity();
        String wsUrl = "ws://localhost:" + port + "/ws";
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompSession = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
            stompSession = null;
        }
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    @Test
    void sendProgress_shouldBeReceivedByWebSocketClient() throws Exception {
        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();

        stompSession.subscribe(processingTopic, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                completableFuture.complete((Map<String, Object>) payload);
            }
        });

        Thread.sleep(300);

        executionTaskEntity.setId(taskId);
        executionTaskEntity.setId(sceneId);
        executionTaskEntity.setStatus(ExecutionTaskStatus.RUNNING);
        executionTaskEntity.setProcessedNodes(7);
        executionTaskEntity.setTotalNodes(10);

        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        Map<String, Object> result = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(sceneId.longValue(), ((Number) result.get("sceneId")).longValue());
        assertEquals(ExecutionTaskStatus.RUNNING.toString(), result.get("status"));
        assertEquals(7, ((Number) result.get("processedNodes")).intValue());
        assertEquals(10, ((Number) result.get("totalNodes")).intValue());
    }

    @Test
    void sendCompleted_shouldBeReceivedByWebSocketClient() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();

        stompSession.subscribe(processingTopic, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                completableFuture.complete((Map<String, Object>) payload);
                latch.countDown();
            }
        });

        Thread.sleep(300);

        executionTaskEntity.setId(taskId);
        executionTaskEntity.setId(sceneId);
        executionTaskEntity.setStatus(ExecutionTaskStatus.COMPLETED);
        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        Map<String, Object> result = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(sceneId.longValue(), ((Number) result.get("sceneId")).longValue());
        assertEquals(ExecutionTaskStatus.COMPLETED.toString(), result.get("status"));
    }

    @Test
    void sendError_shouldBeReceivedByWebSocketClient() throws Exception {
        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();
        String errorMessage = "Test error occurred";

        stompSession.subscribe(processingTopic, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                completableFuture.complete((Map<String, Object>) payload);
            }
        });

        Thread.sleep(300);

        executionTaskEntity.setId(taskId);
        executionTaskEntity.setId(sceneId);
        executionTaskEntity.setStatus(ExecutionTaskStatus.FAILED);
        executionTaskEntity.setErrorMessage(errorMessage);
        notificationService.sendTaskStatus(ExecutionTaskPayload.fromEntity(executionTaskEntity));

        Map<String, Object> result = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(sceneId.longValue(), ((Number) result.get("sceneId")).longValue());
        assertEquals(ExecutionTaskStatus.FAILED.toString(), result.get("status"));
        assertEquals(errorMessage, result.get("errorMessage"));
    }
}
