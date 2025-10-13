package com.example.pixel.config;

import com.example.pixel.common.service.NotificationService;
import com.example.pixel.graph_execution.executor.AsyncGraphExecutor;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import com.example.pixel.graph_execution.executor.LevelGraphExecutor;
import com.example.pixel.graph_execution.executor.SyncGraphExecutor;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import com.example.pixel.node_execution.executor.NodeExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executor;

@Configuration
public class GraphExecutionConfig {

    @Bean
    @ConditionalOnProperty(name = "execution.graph.mode", havingValue = "sync")
    public GraphExecutor syncGraphExecutor(
            NodeExecutor nodeExecutor,
            GraphExecutionService graphExecutionService,
            NotificationService notificationService
    ) {
        return new SyncGraphExecutor(nodeExecutor, graphExecutionService, notificationService);
    }

    @Bean
    @ConditionalOnProperty(name = "execution.graph.mode", havingValue = "async")
    public GraphExecutor asyncGraphExecutor(
            NodeExecutor nodeExecutor,
            GraphExecutionService graphExecutionService,
            NotificationService notificationService,
            Executor graphTaskExecutor
    ) {
        return new AsyncGraphExecutor(nodeExecutor, graphExecutionService, notificationService, graphTaskExecutor);
    }

    @Primary
    @Bean
    @ConditionalOnProperty(name = "execution.graph.mode", havingValue = "level")
    public GraphExecutor levelGraphExecutor(
            NodeExecutor nodeExecutor,
            GraphExecutionService graphExecutionService,
            NotificationService notificationService,
            Executor graphTaskExecutor
    ) {
        return new LevelGraphExecutor(nodeExecutor, graphExecutionService, notificationService, graphTaskExecutor);
    }
}

