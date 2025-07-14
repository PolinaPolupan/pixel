package com.example.mypixel.service;

import com.example.mypixel.config.TestCacheConfig;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class NotificationServiceIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationService notificationService;

    private StompSession stompSession;
    private final Long taskId = 1L;
    private final Long sceneId = 1L;
    private final String processingTopic = "/topic/processing/" + taskId;
    private GraphExecutionTask task;

    @BeforeEach
    void setupConnection() throws ExecutionException, InterruptedException, TimeoutException {
        task = new GraphExecutionTask();
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

        task.setId(taskId);
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setProcessedNodes(7);
        task.setTotalNodes(10);

        notificationService.sendTaskStatus(task);

        Map<String, Object> result = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(sceneId.longValue(), ((Number) result.get("sceneId")).longValue());
        assertEquals(TaskStatus.RUNNING.toString(), result.get("status"));
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

        task.setId(taskId);
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.COMPLETED);
        notificationService.sendTaskStatus(task);

        Map<String, Object> result = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(sceneId.longValue(), ((Number) result.get("sceneId")).longValue());
        assertEquals(TaskStatus.COMPLETED.toString(), result.get("status"));
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

        task.setId(taskId);
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        notificationService.sendTaskStatus(task);

        Map<String, Object> result = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(sceneId.longValue(), ((Number) result.get("sceneId")).longValue());
        assertEquals(TaskStatus.FAILED.toString(), result.get("status"));
        assertEquals(errorMessage, result.get("errorMessage"));
    }
}
