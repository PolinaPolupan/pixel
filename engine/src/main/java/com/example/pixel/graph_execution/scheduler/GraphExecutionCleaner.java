package com.example.pixel.graph_execution.scheduler;

import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@Slf4j
public class GraphExecutionCleaner {

    private final GraphExecutionService graphExecutionService;

    @Scheduled(cron = "${cleanup.schedule}")
    public void cleanupDump() {
        for (var execution: graphExecutionService.getInactive()) {
            log.info("Deleting execution {} with status {}", execution.getId(), execution.getStatus());
            graphExecutionService.delete(execution.getId());
        }
    }
}
