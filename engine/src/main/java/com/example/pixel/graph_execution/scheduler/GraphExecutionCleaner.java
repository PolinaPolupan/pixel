package com.example.pixel.graph_execution.scheduler;

import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
@Slf4j
public class GraphExecutionCleaner {

    private final GraphExecutionService graphExecutionService;

    @Scheduled(cron = "${cleanup.schedule}")
    public void cleanupDump() {
        log.info("Dump cleanup started at {}", LocalDate.now());
        for (var execution: graphExecutionService.getInactive()) {
            graphExecutionService.delete(execution.getId());
        }
    }
}
