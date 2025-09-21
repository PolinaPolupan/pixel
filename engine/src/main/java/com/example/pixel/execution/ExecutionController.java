package com.example.pixel.execution;

import com.example.pixel.execution_task.ExecutionTaskPayload;
import com.example.pixel.scene.ScenePayload;
import com.example.pixel.scene.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/scene")
public class ExecutionController {

    private final ExecutionService executionService;
    private final SceneService sceneService;

    @PostMapping("/")
    public ResponseEntity<ScenePayload> createScene() {
        return ResponseEntity.status(HttpStatus.CREATED).body(sceneService.createScene());
    }

    @PostMapping("/{sceneId}/exec")
    public ResponseEntity<ExecutionTaskPayload> execute(
            @PathVariable Long sceneId,
            @RequestBody ExecutionGraphPayload executionGraphPayload
    ) {
        ExecutionTaskPayload task = executionService.startExecution(executionGraphPayload, sceneId);
        return ResponseEntity.ok(task);
    }
}
