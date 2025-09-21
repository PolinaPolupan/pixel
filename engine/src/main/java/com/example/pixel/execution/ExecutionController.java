package com.example.pixel.execution;

import com.example.pixel.execution_task.ExecutionTaskPayload;
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
    public ResponseEntity<ExecutionGraphPayload> createScene() {
        ExecutionGraphPayload scene = sceneService.createScene();
        sceneService.updateLastAccessed(scene.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(scene);
    }

    @PostMapping("/exec")
    public ResponseEntity<ExecutionTaskPayload> execute(@RequestBody ExecutionGraphRequest executionGraphRequest) {
        sceneService.updateLastAccessed(executionGraphRequest.getId());
        ExecutionTaskPayload task = executionService.startExecution(executionGraphRequest);
        return ResponseEntity.ok(task);
    }
}
