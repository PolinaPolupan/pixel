package com.example.pixel.graph_execution.executor;

import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;

import java.util.concurrent.CompletableFuture;

public interface GraphExecutor {
    CompletableFuture<GraphExecutionPayload> launchExecution(GraphPayload graphPayload, GraphExecutionPayload graphExecutionPayload);
}
