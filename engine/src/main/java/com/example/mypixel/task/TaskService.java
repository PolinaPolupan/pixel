package com.example.mypixel.task;

import com.example.mypixel.execution.Graph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskPayload findTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        return TaskPayload.fromEntity(task);
    }

    @Transactional
    public TaskPayload createTask(Graph graph, Long sceneId) {
        log.debug("Creating task for scene {}", sceneId);
        Task task = new Task();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.PENDING);
        task.setTotalNodes(graph.getNodes().size());
        task.setProcessedNodes(0);
        return TaskPayload.fromEntity(taskRepository.save(task));
    }

    @Transactional
    public void updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        task.setStatus(status);
        if (status == TaskStatus.RUNNING && task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        } else if ((status == TaskStatus.COMPLETED || status == TaskStatus.FAILED)
                && task.getEndTime() == null) {
            task.setEndTime(LocalDateTime.now());
        }
        taskRepository.save(task);
    }

    @Transactional
    public void updateTaskProgress(Long taskId, int processedNodes) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        task.setProcessedNodes(processedNodes);
        taskRepository.save(task);
    }

    @Transactional
    public void markTaskFailed(Long taskId, String errorMessage) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        task.setStatus(TaskStatus.FAILED);
        if (task.getEndTime() == null) task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);
    }
}