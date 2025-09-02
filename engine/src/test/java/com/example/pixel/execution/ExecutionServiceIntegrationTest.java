package com.example.pixel.execution;

import com.example.pixel.common.NotificationService;
import com.example.pixel.config.TestCacheConfig;
import com.example.pixel.exception.InvalidNodeInputException;
import com.example.pixel.exception.StorageFileNotFoundException;
import com.example.pixel.task.TaskService;
import com.example.pixel.file_system.StorageService;
import com.example.pixel.task.TaskPayload;
import com.example.pixel.task.TaskStatus;
import com.example.pixel.node.Node;
import com.example.pixel.node.NodeProcessorService;
import com.example.pixel.util.TestFileUtils;
import com.example.pixel.util.TestGraphFactory;
import com.example.pixel.util.TestcontainersExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(TestcontainersExtension.class)
@Import({TestCacheConfig.class})
@ActiveProfiles("test")
@Tag("integration")
public class ExecutionServiceIntegrationTest {

    @MockitoSpyBean
    private ExecutionService executionService;

    @MockitoSpyBean
    private StorageService storageService;

    @MockitoSpyBean
    private TaskService taskService;

    @MockitoSpyBean
    private NodeProcessorService nodeProcessorService;

    @MockitoSpyBean
    private NotificationService notificationService;

    private final Long sceneId = 1L;

    @BeforeEach
    void setup() {
        TestFileUtils.copyResourcesToDirectory(
                "upload-image-dir/scenes/" + sceneId + "/input/",
                "test-images/Picture1.png",
                "test-images/Picture3.png"
        );
    }

    @Test
    void execute_defaultCase_shouldGenerateOutputFiles() throws Exception {
        ExecutionGraph executionGraph = TestGraphFactory.getDefaultGraph(sceneId);
        int nodeCount = executionGraph.getNodes().size();

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(executionGraph, sceneId);
        TaskPayload completedTask = future.get();

        assertEquals(TaskStatus.COMPLETED, completedTask.getStatus());

        verify(taskService).createTask(executionGraph, sceneId);

        assertTrue(storageService.loadAll("scenes/" + sceneId).toArray().length > 0);
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/processed/filtered_result_Picture1.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/processed/filtered_result_Picture3.png").exists());

        verify(nodeProcessorService, times(nodeCount)).processNode(any(), eq(sceneId), eq(completedTask.getId()));
        verify(notificationService, times(nodeCount + 1)).sendTaskStatus(any());
    }

    @Test
    void execute_whenNodeProcessingFails_shouldMarkTaskAsFailed() throws Exception {
        ExecutionGraph executionGraph = TestGraphFactory.getDefaultGraph(sceneId);
        String errorMessage = "Simulated node processing failure";

        doThrow(new RuntimeException(errorMessage))
                .when(nodeProcessorService).processNode(any(), eq(sceneId), any());

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(executionGraph, sceneId);

        try {
            future.get();
            fail("Should have thrown an exception");
        } catch (ExecutionException e) {
            assertInstanceOf(RuntimeException.class, e.getCause());
        }

        verify(taskService).markTaskFailed(any(), eq(errorMessage));
        verify(notificationService).sendTaskStatus(any());
    }

    @Test
    void execute_withMultipleNodes_shouldUpdateProgressCorrectly() throws Exception {
        ExecutionGraph multiNodeExecutionGraph = TestGraphFactory.getDefaultGraph(sceneId);
        int nodeCount = multiNodeExecutionGraph.getNodes().size();

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(multiNodeExecutionGraph, sceneId);
        future.get();

        verify(nodeProcessorService, times(nodeCount)).processNode(any(), eq(sceneId), any());

        ArgumentCaptor<TaskPayload> progressCaptor = ArgumentCaptor.forClass(TaskPayload.class);
        verify(notificationService, times(nodeCount + 1)).sendTaskStatus(progressCaptor.capture());

        List<TaskPayload> progressUpdates = progressCaptor.getAllValues();
        assertEquals(nodeCount + 1, progressUpdates.size());
    }

    @Test
    void execute_shouldTransitionTaskStatusCorrectly() throws Exception {
        ExecutionGraph executionGraph = TestGraphFactory.getDefaultGraph(sceneId);

        ArgumentCaptor<TaskStatus> statusCaptor = ArgumentCaptor.forClass(TaskStatus.class);

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(executionGraph, sceneId);
        future.get();

        verify(taskService, atLeastOnce()).updateTaskStatus(any(), statusCaptor.capture());
        List<TaskStatus> statusUpdates = statusCaptor.getAllValues();

        assertEquals(TaskStatus.RUNNING, statusUpdates.get(0));
        assertEquals(TaskStatus.COMPLETED, statusUpdates.get(statusUpdates.size()-1));
    }

    @Test
    void executeMultipleGraphs_concurrently_shouldAllComplete() throws Exception {
        int concurrentTasks = 3;
        List<CompletableFuture<TaskPayload>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentTasks; i++) {
            Long sceneId = 100L + i;
            ExecutionGraph executionGraph = TestGraphFactory.getMinimalGraph();
            futures.add(executionService.startExecutionAsync(executionGraph, sceneId));
        }

        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        allDone.get(30, TimeUnit.SECONDS);

        for (CompletableFuture<TaskPayload> future : futures) {
            TaskPayload task = future.get();
            assertEquals(TaskStatus.COMPLETED, task.getStatus());
        }
    }

    @Test
    void execute_withInvalidParams_shouldFailWithProperError() throws Exception {
        List<Node> nodes = new ArrayList<>();
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", List.of());
        gaussianParams.put("sizeX", 2);
        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);
        ExecutionGraph executionGraph = new ExecutionGraph(nodes);

        String expectedErrorMessage = "SizeX must be positive and odd";

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(executionGraph, sceneId);

        try {
            future.get();
            fail("Should have thrown an exception for invalid GaussianBlur parameters");
        } catch (ExecutionException e) {
            assertInstanceOf(InvalidNodeInputException.class, e.getCause());
            assertTrue(e.getCause().getMessage().contains(expectedErrorMessage));

            verify(taskService).markTaskFailed(any(), contains(expectedErrorMessage));
            verify(notificationService).sendTaskStatus(any());
        }
    }

    @Test
    void execute_withInvalidParameterType_shouldFailWithProperError() throws Exception {
        List<Node> nodes = new ArrayList<>();
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", 7);
        gaussianParams.put("sizeX", 5);
        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);
        ExecutionGraph executionGraph = new ExecutionGraph(nodes);

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(executionGraph, sceneId);

        try {
            future.get();
            fail("Should have thrown an exception for invalid node type");
        } catch (ExecutionException e) {
            assertInstanceOf(InvalidNodeInputException.class, e.getCause());

            verify(taskService).markTaskFailed(any(), anyString());
            verify(notificationService).sendTaskStatus(any());
        }
    }

    @Test
    void execute_withNonExistentInputFiles_shouldFailGracefully() throws Exception {
        List<Node> nodes = new ArrayList<>();
        Map<String, Object> gaussianParams = new HashMap<>();
        List<String> files = new ArrayList<>();
        files.add("non-existent");
        gaussianParams.put("files", files);
        gaussianParams.put("sizeX", 5);
        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);
        ExecutionGraph executionGraph = new ExecutionGraph(nodes);

        CompletableFuture<TaskPayload> future = executionService.startExecutionAsync(executionGraph, sceneId);

        try {
            future.get();
        } catch (ExecutionException e) {
            assertInstanceOf(StorageFileNotFoundException.class, e.getCause());

            verify(taskService).markTaskFailed(any(), anyString());
            verify(notificationService).sendTaskStatus(any());
        }
    }
}
