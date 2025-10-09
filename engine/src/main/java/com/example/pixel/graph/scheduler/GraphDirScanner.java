package com.example.pixel.graph.scheduler;

import com.example.pixel.common.integration.NodeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GraphDirScanner {

    private final NodeClient nodeClient;

    @Scheduled(cron = "${scan.directory.schedule}")
    public void scanDir() {
        log.debug("Scanning the dir for new graphs to schedule...");
        nodeClient.loadGraphs();
    }
}
