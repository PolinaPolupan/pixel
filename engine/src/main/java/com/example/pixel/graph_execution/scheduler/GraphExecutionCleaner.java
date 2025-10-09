package com.example.pixel.graph_execution.scheduler;

import com.example.pixel.file_system.service.StorageService;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@Slf4j
public class GraphExecutionCleaner {

    @Value("${dump.directory}")
    private String dumpDir;

    private final GraphExecutionService graphExecutionService;
    private final StorageService storageService;

    @Scheduled(cron = "${cleanup.schedule}")
    public void cleanupDump() {
        for (var execution: graphExecutionService.getInactive()) {
            log.info("Deleting execution {} with status {}", execution.getId(), execution.getStatus());
            graphExecutionService.delete(execution.getId());
            storageService.delete(dumpDir + "/" + execution.getId());
        }
    }
}
