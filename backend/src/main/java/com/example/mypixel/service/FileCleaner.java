package com.example.mypixel.service;

import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.repository.GraphExecutionTaskRepository;
import com.example.mypixel.repository.SceneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FileCleaner {

    private final GraphExecutionTaskRepository taskRepository;
    private final SceneRepository sceneRepository;
    private final StorageService storageService;

    @Autowired
    public FileCleaner(GraphExecutionTaskRepository taskRepository,
                       SceneRepository sceneRepository,
                       StorageService storageService) {
        this.taskRepository = taskRepository;
        this.sceneRepository = sceneRepository;
        this.storageService = storageService;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanupDump() {
        log.info("Cleanup started at {}", LocalDate.now());
        for (var scene: sceneRepository.findAll()) {
            boolean delete = true;
            for (var task: taskRepository.findBySceneId(scene.getId())) {
                if (task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.RUNNING) {
                    delete = false;
                    break;
                }
            }
            if (delete) {
                storageService.delete(scene.getId().toString() + "/temp");
                log.info("Cleanup finished at {}", LocalDate.now());
            }
        }
    }
}
