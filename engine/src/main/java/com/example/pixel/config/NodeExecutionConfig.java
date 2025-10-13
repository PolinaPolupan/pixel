package com.example.pixel.config;

import com.example.pixel.node_execution.executor.AsyncNodeExecutor;
import com.example.pixel.node_execution.executor.NodeExecutor;
import com.example.pixel.node_execution.executor.SyncNodeExecutor;
import com.example.pixel.node_execution.service.NodeExecutionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class NodeExecutionConfig {

    @Bean
    @ConditionalOnProperty(name = "execution.node.mode", havingValue = "sync")
    public NodeExecutor syncNodeExecutor(NodeExecutionService nodeExecutionService) {
        return new SyncNodeExecutor(nodeExecutionService);
    }

    @Primary
    @Bean
    @ConditionalOnProperty(name = "execution.node.mode", havingValue = "async")
    public NodeExecutor asyncNodeExecutor(NodeExecutionService nodeExecutionService) {
        return new AsyncNodeExecutor(nodeExecutionService);
    }
}
