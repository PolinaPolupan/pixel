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
    private final ExecutionService executionService;

    @PostMapping("/")
    public ResponseEntity<ExecutionGraphPayload> create() {
        ExecutionGraphPayload scene = executionService.createExecutionGraph();
        executionService.updateLastAccessed(scene.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(scene);
    }

    @PostMapping("/exec")
    public ResponseEntity<ExecutionTaskPayload> execute(@RequestBody ExecutionGraphRequest executionGraphRequest) {
        executionService.updateLastAccessed(executionGraphRequest.getId());
        ExecutionTaskPayload task = graphExecutor.startExecution(executionGraphRequest);
        return ResponseEntity.ok(task);
    }
}
