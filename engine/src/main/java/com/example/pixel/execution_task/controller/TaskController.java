package com.example.pixel.execution_task.controller;

import com.example.pixel.execution_task.model.ExecutionTaskPayload;
import com.example.pixel.execution_task.service.ExecutionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/task")
public class TaskController {

    private final ExecutionTaskService executionTaskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<ExecutionTaskPayload> getTask(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.OK).body(executionTaskService.findTaskById(taskId));
    }
}
