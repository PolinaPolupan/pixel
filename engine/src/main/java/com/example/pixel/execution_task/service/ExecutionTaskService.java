package com.example.pixel.execution_task.service;

import com.example.pixel.common.exception.TaskNotFoundException;
import com.example.pixel.execution.model.ExecutionGraphRequest;
import com.example.pixel.execution_task.model.ExecutionTaskEntity;
import com.example.pixel.execution_task.model.ExecutionTaskPayload;
import com.example.pixel.execution_task.repository.ExecutionTaskRepository;
import com.example.pixel.execution_task.model.ExecutionTaskStatus;
import com.example.pixel.file_system.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${dump.directory}")
    private String dumpDir;

    @Transactional
    public ExecutionTaskPayload findTaskById(Long taskId) {
        ExecutionTaskEntity executionTaskEntity = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        return ExecutionTaskPayload.fromEntity(executionTaskEntity);
    }

    @Transactional
    public ExecutionTaskPayload createTask(ExecutionGraphRequest executionGraphRequest) {
        log.debug("Creating task for graph {}", executionGraphRequest.getId());
        ExecutionTaskEntity executionTaskEntity = new ExecutionTaskEntity();
        executionTaskEntity.setGraphId(executionGraphRequest.getId());
        executionTaskEntity.setStatus(ExecutionTaskStatus.PENDING);
        executionTaskEntity.setTotalNodes(executionGraphRequest.getNodes().size());
        executionTaskEntity.setProcessedNodes(0);
        return ExecutionTaskPayload.fromEntity(executionTaskRepository.save(executionTaskEntity));
    }

    @Transactional
    public void updateTaskStatus(Long taskId, ExecutionTaskStatus status) {
        ExecutionTaskEntity executionTaskEntity = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        executionTaskEntity.setStatus(status);
        if (status == ExecutionTaskStatus.RUNNING && executionTaskEntity.getStartTime() == null) {
            executionTaskEntity.setStartTime(LocalDateTime.now());
        } else if ((status == ExecutionTaskStatus.COMPLETED || status == ExecutionTaskStatus.FAILED)
                && executionTaskEntity.getEndTime() == null) {
            executionTaskEntity.setEndTime(LocalDateTime.now());
        }
        executionTaskRepository.save(executionTaskEntity);
    }

    @Transactional
    public void updateTaskProgress(Long taskId, int processedNodes) {
        ExecutionTaskEntity executionTaskEntity = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        executionTaskEntity.setProcessedNodes(processedNodes);
        executionTaskRepository.save(executionTaskEntity);
    }

    @Transactional
    public void markTaskFailed(Long taskId, String errorMessage) {
        ExecutionTaskEntity executionTaskEntity = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        executionTaskEntity.setStatus(ExecutionTaskStatus.FAILED);
        if (executionTaskEntity.getEndTime() == null) executionTaskEntity.setEndTime(LocalDateTime.now());
        executionTaskEntity.setErrorMessage(errorMessage);
        executionTaskRepository.save(executionTaskEntity);
    }

    public void delete(Long taskId) {
        ExecutionTaskEntity executionTaskEntity = executionTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        storageService.delete(dumpDir + "/" + taskId);
        executionTaskRepository.delete(executionTaskEntity);
    }

    public List<ExecutionTaskPayload> getInactiveTasks() {
        List<ExecutionTaskEntity> inactiveExecutionTaskEntities = executionTaskRepository.findByStatusNotIn(List.of(ExecutionTaskStatus.PENDING, ExecutionTaskStatus.RUNNING));

        return inactiveExecutionTaskEntities.stream()
                .map(ExecutionTaskPayload::fromEntity)
                .collect(Collectors.toList());
    }
}