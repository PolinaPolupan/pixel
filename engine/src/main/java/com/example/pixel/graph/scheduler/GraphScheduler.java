package com.example.pixel.graph.scheduler;

import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.service.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@RequiredArgsConstructor
@Component
public class GraphScheduler {

    private final TaskScheduler taskScheduler;
    private final GraphService graphService;

    private final Set<Long> scheduledGraphs = ConcurrentHashMap.newKeySet();

    @Scheduled(cron = "${scan.schedule}")
    public void scanAndScheduleGraphs() {
        log.debug("Scanning for new graphs to schedule...");
        graphService.findAll().forEach(this::maybeScheduleGraph);
    }

    private void maybeScheduleGraph(GraphPayload graph) {
        if (scheduledGraphs.contains(graph.getId())) {
            return;
        }
        scheduleGraph(graph);
    }

    private void scheduleGraph(GraphPayload graph) {
        CronTrigger cronTrigger = new CronTrigger(graph.getSchedule());
        taskScheduler.schedule(() -> graphService.execute(graph), cronTrigger);
        scheduledGraphs.add(graph.getId());
        log.info("Scheduled graph id={} with schedule={}", graph.getId(), graph.getSchedule());
    }
}

