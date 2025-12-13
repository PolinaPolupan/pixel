package com.example.pixel.graph_execution.executor;

import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph_execution.dto.GraphExecutionDto;

import java.util.concurrent.CompletableFuture;

public interface GraphExecutor {
    CompletableFuture<GraphExecutionDto> launchExecution(GraphDto graphDto, GraphExecutionDto graphExecutionDto);
}
