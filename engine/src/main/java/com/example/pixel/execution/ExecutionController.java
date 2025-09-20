package com.example.pixel.execution;

import com.example.pixel.execution_task.ExecutionTaskPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/scene/{sceneId}/exec")
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    public ResponseEntity<ExecutionTaskPayload> execute(
            @PathVariable Long sceneId,
            @RequestBody ExecutionGraphPayload executionGraphPayload
    ) {
        ExecutionTaskPayload task = executionService.startExecution(executionGraphPayload, sceneId);
        return ResponseEntity.ok(task);
    }
}
