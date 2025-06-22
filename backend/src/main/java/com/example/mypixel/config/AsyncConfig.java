package com.example.mypixel.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Configuration
@EnableAsync
public class AsyncConfig {

    private final MeterRegistry registry;

    @Bean(name = "graphTaskExecutor")
    public Executor graphTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("GraphExec-");
        executor.initialize();
        return ExecutorServiceMetrics.monitor(registry, executor.getThreadPoolExecutor(),
                "application.tasks",
                "MyPixel application task executor");
    }
}