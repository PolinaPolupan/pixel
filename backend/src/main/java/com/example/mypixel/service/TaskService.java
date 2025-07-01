package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.repository.TaskRepository;
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
    public GraphExecutionTask createTask(Graph graph, Long sceneId) {
        log.debug("Creating task for scene {}", sceneId);
        GraphExecutionTask task = new GraphExecutionTask();
        task.setSceneId(sceneId);
        task.setStatus(TaskStatus.PENDING);
        task.setTotalNodes(graph.getNodes().size());
        task.setProcessedNodes(0);
        return taskRepository.save(task);
    }

    @Transactional
    public void updateTaskStatus(GraphExecutionTask task, TaskStatus status) {
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
    public void updateTaskProgress(GraphExecutionTask task, int processedNodes) {
        task.setProcessedNodes(processedNodes);
        taskRepository.save(task);
    }

    @Transactional
    public void markTaskFailed(GraphExecutionTask task, String errorMessage) {
        task.setStatus(TaskStatus.FAILED);
        if (task.getEndTime() == null) task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);
    }
}