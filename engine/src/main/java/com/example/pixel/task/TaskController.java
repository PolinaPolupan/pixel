package com.example.pixel.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/task")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskPayload> getTask(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.findTaskById(taskId));
    }
}
