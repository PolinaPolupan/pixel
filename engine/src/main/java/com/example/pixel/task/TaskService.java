package com.example.pixel.task;

import com.example.pixel.exception.TaskNotFoundException;
import com.example.pixel.execution.Graph;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final StorageService storageService;

    @Transactional
    public TaskPayload findTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
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
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
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
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        task.setProcessedNodes(processedNodes);
        taskRepository.save(task);
    }

    @Transactional
    public void markTaskFailed(Long taskId, String errorMessage) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        task.setStatus(TaskStatus.FAILED);
        if (task.getEndTime() == null) task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);
    }

    public void delete(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        storageService.delete("tasks/" + taskId);
        taskRepository.delete(task);
    }

    public List<TaskPayload> getInactiveTasks() {
        List<Task> inactiveTasks = taskRepository.findByStatusNotIn(List.of(TaskStatus.PENDING, TaskStatus.RUNNING));

        return inactiveTasks.stream()
                .map(TaskPayload::fromEntity)
                .collect(Collectors.toList());
    }
}