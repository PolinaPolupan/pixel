package com.example.pixel.execution;

import com.example.pixel.execution_task.ExecutionTaskPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/graph")
public class ExecutionController {

    private final GraphExecutor graphExecutor;
    private final GraphService graphService;

    @PostMapping("/")
    public ResponseEntity<ExecutionGraphPayload> create() {
        ExecutionGraphPayload scene = graphService.createExecutionGraph();
        graphService.updateLastAccessed(scene.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(scene);
    }

    @PostMapping("/exec")
    public ResponseEntity<ExecutionTaskPayload> execute(@RequestBody ExecutionGraphRequest executionGraphRequest) {
        graphService.updateLastAccessed(executionGraphRequest.getId());
        ExecutionTaskPayload task = graphExecutor.startExecution(executionGraphRequest);
        return ResponseEntity.ok(task);
    }
}
