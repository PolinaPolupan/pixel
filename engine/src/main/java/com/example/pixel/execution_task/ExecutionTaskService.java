package com.example.pixel.execution_task;

import com.example.pixel.exception.TaskNotFoundException;
import com.example.pixel.execution.ExecutionGraph;
import com.example.pixel.file_system.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionTaskService {

    private final ExecutionTaskRepository executionTaskRepository;
    private final StorageService storageService;

    @Transactional
    public ExecutionTaskPayload findTaskById(Long taskId) {
        ExecutionTask executionTask = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        return ExecutionTaskPayload.fromEntity(executionTask);
    }

    @Transactional
    public ExecutionTaskPayload createTask(ExecutionGraph executionGraph, Long sceneId) {
        log.debug("Creating task for scene {}", sceneId);
        ExecutionTask executionTask = new ExecutionTask();
        executionTask.setSceneId(sceneId);
        executionTask.setStatus(ExecutionTaskStatus.PENDING);
        executionTask.setTotalNodes(executionGraph.getNodes().size());
        executionTask.setProcessedNodes(0);
        return ExecutionTaskPayload.fromEntity(executionTaskRepository.save(executionTask));
    }

    @Transactional
    public void updateTaskStatus(Long taskId, ExecutionTaskStatus status) {
        ExecutionTask executionTask = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        executionTask.setStatus(status);
        if (status == ExecutionTaskStatus.RUNNING && executionTask.getStartTime() == null) {
            executionTask.setStartTime(LocalDateTime.now());
        } else if ((status == ExecutionTaskStatus.COMPLETED || status == ExecutionTaskStatus.FAILED)
                && executionTask.getEndTime() == null) {
            executionTask.setEndTime(LocalDateTime.now());
        }
        executionTaskRepository.save(executionTask);
    }

    @Transactional
    public void updateTaskProgress(Long taskId, int processedNodes) {
        ExecutionTask executionTask = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        executionTask.setProcessedNodes(processedNodes);
        executionTaskRepository.save(executionTask);
    }

    @Transactional
    public void markTaskFailed(Long taskId, String errorMessage) {
        ExecutionTask executionTask = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        executionTask.setStatus(ExecutionTaskStatus.FAILED);
        if (executionTask.getEndTime() == null) executionTask.setEndTime(LocalDateTime.now());
        executionTask.setErrorMessage(errorMessage);
        executionTaskRepository.save(executionTask);
    }

    public void delete(Long taskId) {
        ExecutionTask executionTask = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        storageService.delete("tasks/" + taskId);
        executionTaskRepository.delete(executionTask);
    }

    public List<ExecutionTaskPayload> getInactiveTasks() {
        List<ExecutionTask> inactiveExecutionTasks = executionTaskRepository.findByStatusNotIn(List.of(ExecutionTaskStatus.PENDING, ExecutionTaskStatus.RUNNING));

        return inactiveExecutionTasks.stream()
                .map(ExecutionTaskPayload::fromEntity)
                .collect(Collectors.toList());
    }
}