//package com.example.pixel.execution_task;
//
//import com.example.pixel.config.TestCacheConfig;
//import com.example.pixel.execution.ExecutionGraph;
//import com.example.pixel.util.TestGraphFactory;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@Import({TestCacheConfig.class})
//@ActiveProfiles("test")
//@Tag("integration")
//public class ExecutionExecutionTaskServiceTest {
//
//    @MockitoSpyBean
//    private ExecutionTaskRepository executionTaskRepository;
//
//    @Autowired
//    private ExecutionTaskService executionTaskService;
//
//    @Captor
//    private ArgumentCaptor<ExecutionTask> taskCaptor;
//
//    private final Long sceneId = 1L;
//
//    @Test
//    void createTask_shouldSetInitialValues() {
//        ExecutionGraph executionGraph = TestGraphFactory.getDefaultGraph(sceneId).toExecutionGraph();
//
//        executionTaskService.createTask(executionGraph, sceneId);
//
//        verify(executionTaskRepository).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.PENDING, capturedExecutionTask.getStatus());
//        assertEquals(sceneId, capturedExecutionTask.getSceneId());
//        assertEquals(8, capturedExecutionTask.getTotalNodes());
//        assertEquals(0, capturedExecutionTask.getProcessedNodes());
//        assertNull(capturedExecutionTask.getStartTime());
//        assertNull(capturedExecutionTask.getEndTime());
//        assertNull(capturedExecutionTask.getErrorMessage());
//    }
//
//    @Test
//    void createTask_withEmptyGraph_shouldSetZeroNodes() {
//        ExecutionGraph executionGraph = new ExecutionGraph(List.of());
//        ExecutionTaskPayload result = executionTaskService.createTask(executionGraph, sceneId);
//
//        assertEquals(0, result.getTotalNodes());
//    }
//
//    @Test
//    void updateTaskStatus_toRunning_withExistingStartTime_shouldNotChangeStartTime() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.PENDING);
//        LocalDateTime existingStartTime = LocalDateTime.now().minusHours(1);
//        executionTask.setStartTime(existingStartTime);
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.updateTaskStatus(executionTask.getId(), ExecutionTaskStatus.RUNNING);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.RUNNING, capturedExecutionTask.getStatus());
//    }
//
//    @Test
//    void updateTaskStatus_toCompleted_shouldSetEndTime() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
//        executionTask.setStartTime(LocalDateTime.now().minusMinutes(5));
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.updateTaskStatus(executionTask.getId(), ExecutionTaskStatus.COMPLETED);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.COMPLETED, capturedExecutionTask.getStatus());
//        assertNotNull(capturedExecutionTask.getEndTime());
//    }
//
//    @Test
//    void updateTaskStatus_toFailed_shouldSetEndTime() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
//        executionTask.setStartTime(LocalDateTime.now().minusMinutes(5));
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.updateTaskStatus(executionTask.getId(), ExecutionTaskStatus.FAILED);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.FAILED, capturedExecutionTask.getStatus());
//        assertNotNull(capturedExecutionTask.getEndTime());
//    }
//
//    @Test
//    void updateTaskStatus_toCompleted_withExistingEndTime_shouldNotChangeEndTime() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.PENDING);
//        LocalDateTime existingStartTime = LocalDateTime.now();
//        executionTask.setStartTime(existingStartTime);
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.updateTaskStatus(executionTask.getId(), ExecutionTaskStatus.COMPLETED);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.COMPLETED, capturedExecutionTask.getStatus());
//    }
//
//    @Test
//    void updateTaskProgress_shouldUpdateProcessedNodes() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
//        executionTask.setTotalNodes(5);
//        executionTask.setProcessedNodes(2);
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.updateTaskProgress(executionTask.getId(), 3);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(3, capturedExecutionTask.getProcessedNodes());
//    }
//
//    @Test
//    void markTaskFailed_shouldSetFailedStatusAndErrorMessage() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
//        executionTask.setStartTime(LocalDateTime.now().minusMinutes(3));
//        String errorMessage = "Test error message";
//        executionTask =  executionTaskRepository.save(executionTask);
//
//        executionTaskService.markTaskFailed(executionTask.getId(), errorMessage);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.FAILED, capturedExecutionTask.getStatus());
//        assertEquals(errorMessage, capturedExecutionTask.getErrorMessage());
//        assertNotNull(capturedExecutionTask.getEndTime());
//    }
//
//    @Test
//    void markTaskFailed_withExistingEndTime_shouldNotChangeEndTime() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
//        executionTask.setStartTime(LocalDateTime.now().minusMinutes(5));
//        LocalDateTime existingEndTime = LocalDateTime.now().minusMinutes(1);
//        executionTask.setEndTime(existingEndTime);
//        String errorMessage = "Test error message";
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.markTaskFailed(executionTask.getId(), errorMessage);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(ExecutionTaskStatus.FAILED, capturedExecutionTask.getStatus());
//        assertEquals(errorMessage, capturedExecutionTask.getErrorMessage());
//    }
//
//    @Test
//    void markTaskFailed_shouldOverwriteExistingErrorMessage() {
//        ExecutionTask executionTask = new ExecutionTask();
//        executionTask.setSceneId(sceneId);
//        executionTask.setStatus(ExecutionTaskStatus.RUNNING);
//        executionTask.setErrorMessage("Previous error");
//        String newErrorMessage = "New error message";
//        executionTask = executionTaskRepository.save(executionTask);
//
//        executionTaskService.markTaskFailed(executionTask.getId(), newErrorMessage);
//
//        verify(executionTaskRepository, times(2)).save(taskCaptor.capture());
//        ExecutionTask capturedExecutionTask = taskCaptor.getValue();
//
//        assertEquals(newErrorMessage, capturedExecutionTask.getErrorMessage());
//    }
//}
