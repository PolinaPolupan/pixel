package com.example.pixel.file_system;

import com.example.pixel.task.TaskService;
import com.example.pixel.scene.SceneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class FileCleaner {

    private final SceneService sceneService;
    private final TaskService taskService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanupDump() {
        log.info("Dump cleanup started at {}", LocalDate.now());
        for (var task: taskService.getInactiveTasks()) {
            taskService.delete(task.getId());
        }
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanupScene() {
        log.info("Scene cleanup started at {}", LocalDate.now());
        for (var scene: sceneService.getInactiveScenes()) {
            sceneService.deleteScene(scene.getId());
        }
    }
}
