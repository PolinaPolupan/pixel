package com.example.pixel.execution;

import com.example.pixel.task.TaskPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/scene/{sceneId}/exec")
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    public ResponseEntity<TaskPayload> execute(@PathVariable Long sceneId, @RequestBody Graph graph) {
        TaskPayload task = executionService.startExecution(graph, sceneId);
        return ResponseEntity.ok(task);
    }
}
