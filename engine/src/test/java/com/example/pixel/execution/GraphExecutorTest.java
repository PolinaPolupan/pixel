//package com.example.pixel.execution;
//
//import com.example.pixel.common.service.NotificationService;
//import com.example.pixel.execution_task.service.ExecutionTaskService;
//import com.example.pixel.execution_task.ExecutionTask;
//import com.example.pixel.execution_task.model.ExecutionTaskPayload;
//import com.example.pixel.execution_task.model.ExecutionTaskStatus;
//import com.example.pixel.node.model.Node;
//import com.example.pixel.node.service.impl.NodeProcessorService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.concurrent.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@Slf4j
//@ExtendWith(MockitoExtension.class)
//public class GraphExecutorTest {
//
//    @Mock
//    private NodeProcessorService nodeProcessorService;
//
//    @Mock
//    private ExecutionTaskService executionTaskService;
//
//    @Mock
//    private NotificationService notificationService;
//
//    @InjectMocks
//    private GraphExecutor graphExecutor;
//
//    private ExecutionGraphPayload executionGraph;
//
//    private ExecutionTask executionTask;
//
//    private Node node1, node2;
//
//    private final Long sceneId = 1L;
//    private final Long taskId = 100L;
//
//    @BeforeEach
//    void setup() {
//        node1 = mock(Node.class);
//        node2 = mock(Node.class);
//
//        List<Node> nodes = new ArrayList<>();
//        nodes.add(node1);
//        nodes.add(node2);
//
//        executionGraph = mock(ExecutionGraphPayload.class);
//        Iterator<Node> mockIterator = nodes.iterator();
//
//        executionTask = new ExecutionTask();
//        executionTask.setId(taskId);
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.PENDING);
//        executionTask.setTotalNodes(2);
//        executionTask.setProcessedNodes(0);
//
//        when(executionTaskService.createTask(any(ExecutionGraph.class), anyLong()))
//                .thenReturn(ExecutionTaskPayload.fromEntity(executionTask));
//
//        when(executionTaskService.findTaskById(taskId)).thenReturn(ExecutionTaskPayload.fromEntity(executionTask));
//    }
//
//    @Test
//    void startExecution_shouldCreateTaskAndReturnCompletedTask() throws ExecutionException, InterruptedException {
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//        ExecutionTaskPayload result = future.get();
//
//        verify(executionTaskService).createTask(executionGraph.toExecutionGraph(), sceneId);
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
//        verify(notificationService, times(3)).sendTaskStatus(any(ExecutionTaskPayload.class));
//        assertEquals(ExecutionTaskPayload.fromEntity(executionTask), result);
//    }
//
//    @Test
//    void startExecutionAsync_shouldCreateTaskAndReturnFuture() throws Exception {
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//        ExecutionTaskPayload result = future.get();
//
//        verify(executionTaskService).createTask(executionGraph.toExecutionGraph(), sceneId);
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
//        verify(notificationService, times(3)).sendTaskStatus(any(ExecutionTaskPayload.class));
//        assertEquals(ExecutionTaskPayload.fromEntity(executionTask), result);
//    }
//
//    @Test
//    void executeInternal_shouldProcessAllNodesInOrder() throws Exception {
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//        future.get();
//
//        InOrder inOrder = inOrder(nodeProcessorService, executionTaskService, notificationService);
//
//        inOrder.verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);
//
//        inOrder.verify(nodeProcessorService).processNode(node1, sceneId, taskId);
//        inOrder.verify(executionTaskService).updateTaskProgress(taskId, 1);
//        inOrder.verify(notificationService).sendTaskStatus(any(ExecutionTaskPayload.class));
//
//        inOrder.verify(nodeProcessorService).processNode(node2, sceneId, taskId);
//        inOrder.verify(executionTaskService).updateTaskProgress(taskId, 2);
//        inOrder.verify(notificationService).sendTaskStatus(any(ExecutionTaskPayload.class));
//
//        inOrder.verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
//        inOrder.verify(notificationService).sendTaskStatus(any(ExecutionTaskPayload.class));
//    }
//
//    @Test
//    @MockitoSettings(strictness = Strictness.LENIENT)
//    void executeInternal_whenEmpty_shouldCompleteSuccessfully() throws Exception {
//        ExecutionGraphPayload emptyExecutionGraph = mock(ExecutionGraphPayload.class);
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(emptyExecutionGraph, sceneId);
//        ExecutionTaskPayload result = future.get();
//
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);
//        verify(executionTaskService, never()).updateTaskProgress(any(), anyInt());
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
//        verify(notificationService).sendTaskStatus(any(ExecutionTaskPayload.class));
//        assertEquals(ExecutionTaskPayload.fromEntity(executionTask), result);
//    }
//
//    @Test
//    void executeInternal_whenNodeProcessingFails_shouldMarkTaskFailedAndCompleteFutureExceptionally() {
//        String errorMessage = "Node processing failed";
//        doThrow(new RuntimeException(errorMessage))
//                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//
//        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
//        assertInstanceOf(RuntimeException.class, exception.getCause());
//        assertEquals(errorMessage, exception.getCause().getMessage());
//
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.RUNNING);
//        verify(executionTaskService).markTaskFailed(taskId, errorMessage);
//        verify(notificationService, atLeastOnce()).sendTaskStatus(any(ExecutionTaskPayload.class));
//        verify(executionTaskService, never()).updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
//    }
//
//    @Test
//    void executeInternal_shouldUpdateProgressCorrectly() throws Exception {
//        List<Node> manyNodes = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            manyNodes.add(mock(Node.class));
//        }
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//        future.get();
//
//        ArgumentCaptor<Integer> progressCaptor = ArgumentCaptor.forClass(Integer.class);
//        verify(executionTaskService, times(5)).updateTaskProgress(eq(taskId), progressCaptor.capture());
//
//        List<Integer> progressUpdates = progressCaptor.getAllValues();
//        assertEquals(5, progressUpdates.size());
//
//        for (int i = 0; i < 5; i++) {
//            assertEquals(i + 1, progressUpdates.get(i).intValue());
//        }
//
//        ArgumentCaptor<ExecutionTaskPayload> notificationProgressCaptor = ArgumentCaptor.forClass(ExecutionTaskPayload.class);
//        verify(notificationService, times(6)).sendTaskStatus(notificationProgressCaptor.capture());
//
//        List<ExecutionTaskPayload> notificationProgress = notificationProgressCaptor.getAllValues();
//        assertEquals(6, notificationProgress.size());
//    }
//
//    @Test
//    @MockitoSettings(strictness = Strictness.LENIENT)
//    void startExecution_shouldHandleExceptionDuringExecution() {
//        String errorMessage = "Task creation failed";
//        when(executionTaskService.createTask(any(), anyLong()))
//                .thenThrow(new RuntimeException(errorMessage));
//
////        Exception exception = assertThrows(RuntimeException.class, () ->
////                executionService.startExecution(executionGraph, sceneId));
////
////        assertEquals(errorMessage, exception.getMessage());
//    }
//
//    @Test
//    @MockitoSettings(strictness = Strictness.LENIENT)
//    void startExecutionAsync_shouldHandleExceptionInTaskCreation() {
//        String errorMessage = "Task creation failed";
//        when(executionTaskService.createTask(any(), anyLong()))
//                .thenThrow(new RuntimeException(errorMessage));
//
//        Exception exception = assertThrows(RuntimeException.class, () ->
//                graphExecutor.startExecutionAsync(executionGraph, sceneId));
//
//        assertEquals(errorMessage, exception.getMessage());
//    }
//
//    @Test
//    @MockitoSettings(strictness = Strictness.LENIENT)
//    void executeInternal_shouldHandleInterruptedException() {
//        doThrow(new RuntimeException("Thread interrupted"))
//                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//
//        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
//        assertInstanceOf(RuntimeException.class, exception.getCause());
//
//        verify(executionTaskService).markTaskFailed(eq(taskId), contains("Thread interrupted"));
//        verify(notificationService, atLeastOnce()).sendTaskStatus(any(ExecutionTaskPayload.class));
//    }
//
//    @Test
//    void executeInternal_shouldLogErrorDetails() throws Exception {
//        String errorMessage = "Test error message";
//        RuntimeException testException = new RuntimeException(errorMessage);
//        doThrow(testException)
//                .when(nodeProcessorService).processNode(any(), anyLong(), anyLong());
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//
//        try {
//            future.get();
//        } catch (ExecutionException expected) {
//        }
//
//        verify(executionTaskService).markTaskFailed(eq(taskId), eq(errorMessage));
//        verify(notificationService, atLeastOnce()).sendTaskStatus(any(ExecutionTaskPayload.class));
//    }
//
//    @Test
//    @MockitoSettings(strictness = Strictness.LENIENT)
//    void executeInternal_whenTaskStatusUpdateFails_shouldHandleGracefully() {
//        doThrow(new RuntimeException("Status update failed"))
//                .when(executionTaskService).updateTaskStatus(any(), eq(ExecutionTaskStatus.RUNNING));
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//
//        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
//        assertInstanceOf(RuntimeException.class, exception.getCause());
//        assertEquals("Status update failed", exception.getCause().getMessage());
//
//        verify(executionTaskService).markTaskFailed(eq(taskId), contains("Status update failed"));
//        verify(notificationService, atLeastOnce()).sendTaskStatus(any(ExecutionTaskPayload.class));
//
//        verify(nodeProcessorService, never()).processNode(any(), anyLong(), anyLong());
//    }
//
//    @Test
//    void executeInternal_whenNotificationFails_shouldContinueExecution() throws Exception {
//        doAnswer(invocation -> {
//            log.error("Failed to send progress WebSocket: Notification failed");
//            return null;
//        }).when(notificationService).sendTaskStatus(any(ExecutionTaskPayload.class));
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionSync(executionGraph, sceneId);
//        ExecutionTaskPayload result = future.get();
//
//        verify(executionTaskService).updateTaskStatus(taskId, ExecutionTaskStatus.COMPLETED);
//        verify(nodeProcessorService, times(2)).processNode(any(), anyLong(), anyLong());
//        assertEquals(ExecutionTaskPayload.fromEntity(executionTask), result);
//    }
//}