//package com.example.pixel.execution_graph;
//
//import com.example.pixel.common.service.NotificationService;
//import com.example.pixel.config.TestCacheConfig;
//import com.example.pixel.common.exception.InvalidNodeInputException;
//import com.example.pixel.common.exception.StorageFileNotFoundException;
//import com.example.pixel.execution_graph.dto.GraphPayload;
//import com.example.pixel.execution_graph.service.GraphExecutor;
//import com.example.pixel.execution_task.service.ExecutionTaskService;
//import com.example.pixel.file_system.service.StorageService;
//import com.example.pixel.execution_task.dto.ExecutionTaskPayload;
//import com.example.pixel.execution_task.dto.ExecutionTaskStatus;
//import com.example.pixel.node.model.Node;
//import com.example.pixel.node.service.NodeProcessorService;
//import com.example.pixel.util.TestFileUtils;
//import com.example.pixel.util.TestGraphFactory;
//import com.example.pixel.util.TestcontainersExtension;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Fail.fail;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@ExtendWith(TestcontainersExtension.class)
//@Import({TestCacheConfig.class})
//@ActiveProfiles("test")
//@Tag("integration")
//public class GraphExecutorIntegrationTest {
//
//    @MockitoSpyBean
//    private GraphExecutor graphExecutor;
//
//    @MockitoSpyBean
//    private StorageService storageService;
//
//    @MockitoSpyBean
//    private ExecutionTaskService executionTaskService;
//
//    @MockitoSpyBean
//    private NodeProcessorService nodeProcessorService;
//
//    @MockitoSpyBean
//    private NotificationService notificationService;
//
//    private final Long sceneId = 1L;
//
//    @BeforeEach
//    void setup() {
//        TestFileUtils.copyResourcesToDirectory(
//                "upload-image-dir/scenes/" + sceneId + "/input/",
//                "test-images/Picture1.png",
//                "test-images/Picture3.png"
//        );
//    }
//
//    @Test
//    void execute_defaultCase_shouldGenerateOutputFiles() throws Exception {
//        GraphPayload executionGraph = TestGraphFactory.getDefaultGraph(sceneId);
//        int nodeCount = executionGraph.getNodes().size();
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(executionGraph.);
//        ExecutionTaskPayload completedTask = future.get();
//
//        assertEquals(ExecutionTaskStatus.COMPLETED, completedTask.getStatus());
//
//
//        assertTrue(storageService.loadAll("scenes/" + sceneId).toArray().length > 0);
//        assertTrue(storageService.loadAsResource("scenes/" +
//                sceneId + "/processed/filtered_result_Picture1.png").exists());
//        assertTrue(storageService.loadAsResource("scenes/" +
//                sceneId + "/processed/filtered_result_Picture3.png").exists());
//
//        verify(nodeProcessorService, times(nodeCount)).processNode(any(), eq(sceneId), eq(completedTask.getId()));
//        verify(notificationService, times(nodeCount + 1)).sendTaskStatus(any());
//    }
//
//    @Test
//    void execute_whenNodeProcessingFails_shouldMarkTaskAsFailed() throws Exception {
//        GraphPayload executionGraph = TestGraphFactory.getDefaultGraph(sceneId);
//        String errorMessage = "Simulated node processing failure";
//
//        doThrow(new RuntimeException(errorMessage))
//                .when(nodeProcessorService).processNode(any(), eq(sceneId), any());
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(executionGraph, sceneId);
//
//        try {
//            future.get();
//            fail("Should have thrown an exception");
//        } catch (ExecutionException e) {
//            assertInstanceOf(RuntimeException.class, e.getCause());
//        }
//
//        verify(executionTaskService).markTaskFailed(any(), eq(errorMessage));
//        verify(notificationService).sendTaskStatus(any());
//    }
//
//    @Test
//    void execute_withMultipleNodes_shouldUpdateProgressCorrectly() throws Exception {
//        GraphPayload multiNodeExecutionGraph = TestGraphFactory.getDefaultGraph(sceneId);
//        int nodeCount = multiNodeExecutionGraph.getNodes().size();
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(multiNodeExecutionGraph, sceneId);
//        future.get();
//
//        verify(nodeProcessorService, times(nodeCount)).processNode(any(), eq(sceneId), any());
//
//        ArgumentCaptor<ExecutionTaskPayload> progressCaptor = ArgumentCaptor.forClass(ExecutionTaskPayload.class);
//        verify(notificationService, times(nodeCount + 1)).sendTaskStatus(progressCaptor.capture());
//
//        List<ExecutionTaskPayload> progressUpdates = progressCaptor.getAllValues();
//        assertEquals(nodeCount + 1, progressUpdates.size());
//    }
//
//    @Test
//    void execute_shouldTransitionTaskStatusCorrectly() throws Exception {
//        GraphPayload executionGraph = TestGraphFactory.getDefaultGraph(sceneId);
//
//        ArgumentCaptor<ExecutionTaskStatus> statusCaptor = ArgumentCaptor.forClass(ExecutionTaskStatus.class);
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(executionGraph, sceneId);
//        future.get();
//
//        verify(executionTaskService, atLeastOnce()).updateTaskStatus(any(), statusCaptor.capture());
//        List<ExecutionTaskStatus> statusUpdates = statusCaptor.getAllValues();
//
//        assertEquals(ExecutionTaskStatus.RUNNING, statusUpdates.get(0));
//        assertEquals(ExecutionTaskStatus.COMPLETED, statusUpdates.get(statusUpdates.size()-1));
//    }
//
//    @Test
//    void executeMultipleGraphs_concurrently_shouldAllComplete() throws Exception {
//        int concurrentTasks = 3;
//        List<CompletableFuture<ExecutionTaskPayload>> futures = new ArrayList<>();
//
//        for (int i = 0; i < concurrentTasks; i++) {
//            Long sceneId = 100L + i;
//            GraphPayload executionGraph = TestGraphFactory.getMinimalGraph();
//            futures.add(graphExecutor.startExecutionAsync(executionGraph, sceneId));
//        }
//
//        CompletableFuture<Void> allDone = CompletableFuture.allOf(
//                futures.toArray(new CompletableFuture[0]));
//        allDone.get(30, TimeUnit.SECONDS);
//
//        for (CompletableFuture<ExecutionTaskPayload> future : futures) {
//            ExecutionTaskPayload task = future.get();
//            assertEquals(ExecutionTaskStatus.COMPLETED, task.getStatus());
//        }
//    }
//
//    @Test
//    void execute_withInvalidParams_shouldFailWithProperError() throws Exception {
//        List<Node> nodes = new ArrayList<>();
//        Map<String, Object> gaussianParams = new HashMap<>();
//        gaussianParams.put("files", List.of());
//        gaussianParams.put("sizeX", 2);
//        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
//        nodes.add(gaussianNode);
//        GraphPayload executionGraph = new GraphPayload(nodes);
//
//        String expectedErrorMessage = "SizeX must be positive and odd";
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(executionGraph, sceneId);
//
//        try {
//            future.get();
//            fail("Should have thrown an exception for invalid GaussianBlur parameters");
//        } catch (ExecutionException e) {
//            assertInstanceOf(InvalidNodeInputException.class, e.getCause());
//            assertTrue(e.getCause().getMessage().contains(expectedErrorMessage));
//
//            verify(executionTaskService).markTaskFailed(any(), contains(expectedErrorMessage));
//            verify(notificationService).sendTaskStatus(any());
//        }
//    }
//
//    @Test
//    void execute_withInvalidParameterType_shouldFailWithProperError() throws Exception {
//        List<Node> nodes = new ArrayList<>();
//        Map<String, Object> gaussianParams = new HashMap<>();
//        gaussianParams.put("files", 7);
//        gaussianParams.put("sizeX", 5);
//        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
//        nodes.add(gaussianNode);
//        GraphPayload executionGraph = new GraphPayload(nodes);
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(executionGraph, sceneId);
//
//        try {
//            future.get();
//            fail("Should have thrown an exception for invalid node type");
//        } catch (ExecutionException e) {
//            assertInstanceOf(InvalidNodeInputException.class, e.getCause());
//
//            verify(executionTaskService).markTaskFailed(any(), anyString());
//            verify(notificationService).sendTaskStatus(any());
//        }
//    }
//
//    @Test
//    void execute_withNonExistentInputFiles_shouldFailGracefully() throws Exception {
//        List<Node> nodes = new ArrayList<>();
//        Map<String, Object> gaussianParams = new HashMap<>();
//        List<String> files = new ArrayList<>();
//        files.add("non-existent");
//        gaussianParams.put("files", files);
//        gaussianParams.put("sizeX", 5);
//        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
//        nodes.add(gaussianNode);
//        GraphPayload executionGraph = new GraphPayload(nodes);
//
//        CompletableFuture<ExecutionTaskPayload> future = graphExecutor.startExecutionAsync(executionGraph, sceneId);
//
//        try {
//            future.get();
//        } catch (ExecutionException e) {
//            assertInstanceOf(StorageFileNotFoundException.class, e.getCause());
//
//            verify(executionTaskService).markTaskFailed(any(), anyString());
//            verify(notificationService).sendTaskStatus(any());
//        }
//    }
//}
