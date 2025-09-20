package com.example.pixel.file_system;

import com.example.pixel.execution_task.ExecutionTaskService;
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

    private final ExecutionTaskService executionTaskService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanupDump() {
        log.info("Dump cleanup started at {}", LocalDate.now());
        for (var task: executionTaskService.getInactiveTasks()) {
            executionTaskService.delete(task.getId());
        }
    }
}
