package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.model.node.Node;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GraphServiceTests {

    @Mock
    private NodeProcessorService nodeProcessorService;

    @Mock
    private PerformanceTracker performanceTracker;

    @Mock
    private TaskService taskService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GraphService graphService;

    @Mock
    private Graph graph;

    @Mock
    private GraphExecutionTask task;

    @Mock
    private Node node1, node2;

    private final Long sceneId = 1L;
    private final Long taskId = 100L;

    @BeforeEach
    void setup() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);

        when(task.getId()).thenReturn(taskId);

        Iterator<Node> mockIterator = nodes.iterator();
        when(graph.iterator()).thenReturn(mockIterator);

        when(performanceTracker.trackOperation(anyString(), any(Tags.class), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(2);
                    return supplier.get();
                });

        when(taskService.createTask(any(Graph.class), anyLong()))
                .thenReturn(task);
    }

    @Test
    void startGraphExecution_shouldCreateTaskAndReturnCompletedTask() {
        GraphExecutionTask result = graphService.startGraphExecution(graph, sceneId);

        verify(taskService).createTask(graph, sceneId);
        verify(taskService).updateTaskStatus(task, TaskStatus.RUNNING);
        verify(taskService).updateTaskStatus(task, TaskStatus.COMPLETED);
        verify(notificationService, times(3)).sendTaskStatus(task);
        assertEquals(task, result);
    }

    @Test
    void startGraphExecutionAsync_shouldCreateTaskAndReturnFuture() throws Exception {
        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);
        GraphExecutionTask result = future.get();

        verify(taskService).createTask(graph, sceneId);
        verify(taskService).updateTaskStatus(task, TaskStatus.RUNNING);
        verify(taskService).updateTaskStatus(task, TaskStatus.COMPLETED);
        verify(notificationService, times(3)).sendTaskStatus(task);
        assertEquals(task, result);
    }

    @Test
    void executeGraphInternal_shouldProcessAllNodesInOrder() throws Exception {
        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);
        future.get();

        InOrder inOrder = inOrder(nodeProcessorService, taskService, notificationService);

        inOrder.verify(taskService).updateTaskStatus(task, TaskStatus.RUNNING);

        inOrder.verify(nodeProcessorService).processNode(node1, sceneId, taskId);
        inOrder.verify(taskService).updateTaskProgress(task, 1);
        inOrder.verify(notificationService).sendTaskStatus(task);

        inOrder.verify(nodeProcessorService).processNode(node2, sceneId, taskId);
        inOrder.verify(taskService).updateTaskProgress(task, 2);
        inOrder.verify(notificationService).sendTaskStatus(task);

        inOrder.verify(taskService).updateTaskStatus(task, TaskStatus.COMPLETED);
        inOrder.verify(notificationService).sendTaskStatus(task);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraphInternal_whenEmptyGraph_shouldCompleteSuccessfully() throws Exception {
        Graph emptyGraph = mock(Graph.class);
        List<Node> emptyList = new ArrayList<>();
        when(emptyGraph.getNodes()).thenReturn(emptyList);
        when(emptyGraph.iterator()).thenReturn(emptyList.iterator());

        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(emptyGraph, sceneId);
        GraphExecutionTask result = future.get();

        verify(taskService).updateTaskStatus(task, TaskStatus.RUNNING);
        verify(taskService, never()).updateTaskProgress(any(), anyInt());
        verify(taskService).updateTaskStatus(task, TaskStatus.COMPLETED);
        verify(notificationService).sendTaskStatus(task);
        assertEquals(task, result);
    }

    @Test
    void executeGraphInternal_whenNodeProcessingFails_shouldMarkTaskFailedAndCompleteFutureExceptionally() {
        String errorMessage = "Node processing failed";
        doThrow(new RuntimeException(errorMessage))
                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());

        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals(errorMessage, exception.getCause().getMessage());

        verify(taskService).updateTaskStatus(task, TaskStatus.RUNNING);
        verify(taskService).markTaskFailed(task, errorMessage);
        verify(notificationService).sendTaskStatus(task);
        verify(taskService, never()).updateTaskStatus(task, TaskStatus.COMPLETED);
    }

    @Test
    void executeGraphInternal_shouldUpdateProgressCorrectly() throws Exception {
        List<Node> manyNodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            manyNodes.add(mock(Node.class));
        }

        when(graph.iterator()).thenReturn(manyNodes.iterator());

        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);
        future.get();

        ArgumentCaptor<Integer> progressCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(taskService, times(5)).updateTaskProgress(eq(task), progressCaptor.capture());

        List<Integer> progressUpdates = progressCaptor.getAllValues();
        assertEquals(5, progressUpdates.size());

        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, progressUpdates.get(i).intValue());
        }

        ArgumentCaptor<GraphExecutionTask> notificationProgressCaptor = ArgumentCaptor.forClass(GraphExecutionTask.class);
        verify(notificationService, times(6)).sendTaskStatus(notificationProgressCaptor.capture());

        List<GraphExecutionTask> notificationProgress = notificationProgressCaptor.getAllValues();
        assertEquals(6, notificationProgress.size());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void startGraphExecution_shouldHandleExceptionDuringExecution() {
        String errorMessage = "Task creation failed";
        when(taskService.createTask(any(), anyLong()))
                .thenThrow(new RuntimeException(errorMessage));

        Exception exception = assertThrows(RuntimeException.class, () ->
                graphService.startGraphExecution(graph, sceneId));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void startGraphExecutionAsync_shouldHandleExceptionInTaskCreation() {
        String errorMessage = "Task creation failed";
        when(taskService.createTask(any(), anyLong()))
                .thenThrow(new RuntimeException(errorMessage));

        Exception exception = assertThrows(RuntimeException.class, () ->
                graphService.startGraphExecutionAsync(graph, sceneId));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraph_shouldUsePerformanceTracker() {
        reset(performanceTracker);
        CompletableFuture<GraphExecutionTask> expectedFuture = CompletableFuture.completedFuture(task);

        when(performanceTracker.trackOperation(anyString(), any(Tags.class), any(Supplier.class)))
                .thenReturn(expectedFuture);

        CompletableFuture<GraphExecutionTask> actualFuture = graphService.executeGraph(graph, task, sceneId);

        verify(performanceTracker).trackOperation(
                eq("graph.execution"),
                eq(Tags.of("scene.id", String.valueOf(sceneId))),
                any(Supplier.class)
        );

        assertSame(expectedFuture, actualFuture);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraphInternal_shouldHandleInterruptedException() {
        doThrow(new RuntimeException("Thread interrupted"))
                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());

        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());

        verify(taskService).markTaskFailed(eq(task), contains("Thread interrupted"));
        verify(notificationService).sendTaskStatus(task);
    }

    @Test
    void executeGraphInternal_shouldLogErrorDetails() {
        String errorMessage = "Test error message";
        RuntimeException testException = new RuntimeException(errorMessage);
        doThrow(testException)
                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());

        graphService.startGraphExecutionAsync(graph, sceneId);

        verify(taskService).markTaskFailed(eq(task), eq(errorMessage));
        verify(notificationService).sendTaskStatus(task);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void executeGraphInternal_whenTaskStatusUpdateFails_shouldHandleGracefully() {
        doThrow(new RuntimeException("Status update failed"))
                .when(taskService).updateTaskStatus(any(), eq(TaskStatus.RUNNING));

        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Status update failed", exception.getCause().getMessage());

        verify(taskService).markTaskFailed(eq(task), contains("Status update failed"));
        verify(notificationService).sendTaskStatus(task);

        verify(nodeProcessorService, never()).processNode(any(), anyLong(), anyLong());
    }

    @Test
    void executeGraphInternal_whenNotificationFails_shouldContinueExecution() throws Exception {
        doAnswer(invocation -> {
            log.error("Failed to send progress WebSocket: Notification failed");
            return null;
        }).when(notificationService).sendTaskStatus(task);

        CompletableFuture<GraphExecutionTask> future = graphService.startGraphExecutionAsync(graph, sceneId);
        GraphExecutionTask result = future.get();

        verify(taskService).updateTaskStatus(task, TaskStatus.COMPLETED);
        verify(nodeProcessorService, times(2)).processNode(any(), anyLong(), anyLong());
        assertEquals(task, result);
    }
}