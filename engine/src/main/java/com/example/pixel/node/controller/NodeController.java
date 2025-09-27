package com.example.pixel.node.controller;

import com.example.pixel.node.dto.NodeConfiguration;
import com.example.pixel.node.entity.NodeEntity;
import com.example.pixel.node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/node")
public class NodeController {

    private final NodeService nodeService;

    @PostMapping
    public ResponseEntity<NodeEntity> create(@RequestBody NodeConfiguration nodeConfiguration) {
        return ResponseEntity.ok(nodeService.create(nodeConfiguration));
    }

    @GetMapping
    public ResponseEntity<Map<String, NodeConfiguration>> getAllNodes() {
        return ResponseEntity.ok(nodeService.getAllActiveNodes());
    }
}
