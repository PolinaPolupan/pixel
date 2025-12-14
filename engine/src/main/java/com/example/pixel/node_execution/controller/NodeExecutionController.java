package com.example.pixel.node_execution.controller;

import com.example.pixel.node_execution.dto.NodeExecutionDto;
import com.example.pixel.node_execution.service.NodeExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/node_execution")
public class NodeExecutionController {

    private final NodeExecutionService nodeExecutionService;

    @GetMapping
    public ResponseEntity<List<NodeExecutionDto>> getAll(@RequestParam(required = false) Long graphExecutionId) {
        if (graphExecutionId != null) {
            return ResponseEntity.ok(nodeExecutionService.findByGraphExecutionId(graphExecutionId));
        }
        return ResponseEntity.ok(nodeExecutionService.findAll());
    }
}
