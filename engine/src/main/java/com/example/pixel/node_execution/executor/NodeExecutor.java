package com.example.pixel.node_execution.executor;

import com.example.pixel.node_execution.dto.NodeExecutionDto;
import com.example.pixel.node_execution.model.Node;

import java.util.concurrent.CompletableFuture;

public interface NodeExecutor {
    CompletableFuture<NodeExecutionDto> launchExecution(Node node, Long graphExecutionId);
}
