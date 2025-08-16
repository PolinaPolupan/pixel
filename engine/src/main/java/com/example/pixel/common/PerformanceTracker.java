package com.example.pixel.common;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceTracker {
    private final MeterRegistry meterRegistry;
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    public void trackOperation(String operationName, Tags tags, Runnable operation) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        long memoryBefore = getUsedMemory();

        try {
            operation.run();
        } finally {
            collectMetrics(operationName, tags, timerSample, memoryBefore);
        }
    }

    public <T> T trackOperation(String operationName, Tags tags, Supplier<T> operation) {
        Timer.Sample timerSample = Timer.start(meterRegistry);
        long memoryBefore = getUsedMemory();

        try {
            return operation.get();
        } finally {
            collectMetrics(operationName, tags, timerSample, memoryBefore);
        }
    }

    private void collectMetrics(String operationName, Tags tags, Timer.Sample timerSample, long memoryBefore) {
        timerSample.stop(Timer.builder(operationName + ".time")
                .tags(tags)
                .description("Execution time for " + operationName)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));

        long memoryAfter = getUsedMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        meterRegistry.gauge(operationName + ".memory.used",
                Tags.concat(tags, "unit", "bytes"),
                memoryUsed);

        log.debug("{} completed - Time: {} ms, Memory used: {} bytes",
                operationName,
                timerSample.stop(meterRegistry.timer(operationName + ".debug.time")),
                memoryUsed);
    }

    private long getUsedMemory() {
        return memoryMXBean.getHeapMemoryUsage().getUsed() +
                memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }
}
