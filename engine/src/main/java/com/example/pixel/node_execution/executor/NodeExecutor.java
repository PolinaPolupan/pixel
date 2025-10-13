package com.example.pixel.node_execution.executor;

import com.example.pixel.node_execution.dto.NodeExecutionPayload;
import com.example.pixel.node_execution.model.NodeExecution;

import java.util.concurrent.CompletableFuture;

public interface NodeExecutor {
    CompletableFuture<NodeExecutionPayload> launchExecution(NodeExecution nodeExecution, Long graphExecutionId);
}
