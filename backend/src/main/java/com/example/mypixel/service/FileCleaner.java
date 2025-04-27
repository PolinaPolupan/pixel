package com.example.mypixel.service;

import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.repository.GraphExecutionTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class FileCleaner {

    private final SceneService sceneService;
    private final GraphExecutionTaskRepository taskRepository;
    private final StorageService storageService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanupDump() {
        log.info("Dump cleanup started at {}", LocalDate.now());
        for (var scene: taskRepository.findByStatusNotIn(List.of(TaskStatus.PENDING, TaskStatus.RUNNING))) {
            storageService.delete(scene.getId().toString() + "/temp");
            log.info("Dump cleanup of scene {} finished at {}", scene.getId(), LocalDate.now());
        }
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanupScene() {
        log.info("Scene cleanup started at {}", LocalDate.now());
        for (var scene: sceneService.getInactiveScenes()) {
            log.info("Scene with id {} cleanup finished at {}", scene.getId(),LocalDate.now());
            sceneService.deleteScene(scene.getId());
        }
    }
}
