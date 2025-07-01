package com.example.mypixel.service;

import com.example.mypixel.config.TestCacheConfig;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.repository.TaskRepository;
import com.example.mypixel.util.TestGraphFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import({TestCacheConfig.class})
@ActiveProfiles("test")
public class TaskServiceTests {

    @MockitoSpyBean
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Captor
    private ArgumentCaptor<GraphExecutionTask> taskCaptor;

    private final Long sceneId = 1L;

    @Test
    void createTask_shouldSetInitialValues() {
        Graph graph = TestGraphFactory.getDefaultGraph(sceneId);

        taskService.createTask(graph, sceneId);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.PENDING, capturedTask.getStatus());
        assertEquals(sceneId, capturedTask.getSceneId());
        assertEquals(8, capturedTask.getTotalNodes());
        assertEquals(0, capturedTask.getProcessedNodes());
        assertNull(capturedTask.getStartTime());
        assertNull(capturedTask.getEndTime());
        assertNull(capturedTask.getErrorMessage());
    }

    @Test
    void createTask_withEmptyGraph_shouldSetZeroNodes() {
        Graph graph = new Graph(List.of());
        GraphExecutionTask result = taskService.createTask(graph, sceneId);

        assertEquals(0, result.getTotalNodes());
    }

    @Test
    void updateTaskStatus_toRunning_withExistingStartTime_shouldNotChangeStartTime() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.PENDING);
        LocalDateTime existingStartTime = LocalDateTime.now().minusHours(1);
        task.setStartTime(existingStartTime);

        taskService.updateTaskStatus(task, TaskStatus.RUNNING);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.RUNNING, capturedTask.getStatus());
        assertEquals(existingStartTime, capturedTask.getStartTime());
    }

    @Test
    void updateTaskStatus_toCompleted_shouldSetEndTime() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(LocalDateTime.now().minusMinutes(5));

        taskService.updateTaskStatus(task, TaskStatus.COMPLETED);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.COMPLETED, capturedTask.getStatus());
        assertNotNull(capturedTask.getEndTime());
    }

    @Test
    void updateTaskStatus_toFailed_shouldSetEndTime() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(LocalDateTime.now().minusMinutes(5));

        taskService.updateTaskStatus(task, TaskStatus.FAILED);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.FAILED, capturedTask.getStatus());
        assertNotNull(capturedTask.getEndTime());
    }

    @Test
    void updateTaskStatus_toCompleted_withExistingEndTime_shouldNotChangeEndTime() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(LocalDateTime.now().minusMinutes(10));
        LocalDateTime existingEndTime = LocalDateTime.now().minusMinutes(2);
        task.setEndTime(existingEndTime);

        taskService.updateTaskStatus(task, TaskStatus.COMPLETED);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.COMPLETED, capturedTask.getStatus());
        assertEquals(existingEndTime, capturedTask.getEndTime());
    }

    @Test
    void updateTaskProgress_shouldUpdateProcessedNodes() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setTotalNodes(5);
        task.setProcessedNodes(2);

        taskService.updateTaskProgress(task, 3);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(3, capturedTask.getProcessedNodes());
    }

    @Test
    void markTaskFailed_shouldSetFailedStatusAndErrorMessage() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(LocalDateTime.now().minusMinutes(3));
        String errorMessage = "Test error message";

        taskService.markTaskFailed(task, errorMessage);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.FAILED, capturedTask.getStatus());
        assertEquals(errorMessage, capturedTask.getErrorMessage());
        assertNotNull(capturedTask.getEndTime());
    }

    @Test
    void markTaskFailed_withExistingEndTime_shouldNotChangeEndTime() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(LocalDateTime.now().minusMinutes(5));
        LocalDateTime existingEndTime = LocalDateTime.now().minusMinutes(1);
        task.setEndTime(existingEndTime);
        String errorMessage = "Test error message";

        taskService.markTaskFailed(task, errorMessage);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(TaskStatus.FAILED, capturedTask.getStatus());
        assertEquals(errorMessage, capturedTask.getErrorMessage());
        assertEquals(existingEndTime, capturedTask.getEndTime());
    }

    @Test
    void markTaskFailed_shouldOverwriteExistingErrorMessage() {
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.RUNNING);
        task.setErrorMessage("Previous error");
        String newErrorMessage = "New error message";

        taskService.markTaskFailed(task, newErrorMessage);

        verify(taskRepository).save(taskCaptor.capture());
        GraphExecutionTask capturedTask = taskCaptor.getValue();

        assertEquals(newErrorMessage, capturedTask.getErrorMessage());
    }
}
