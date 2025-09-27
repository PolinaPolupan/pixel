package com.example.pixel.node.controller;

import com.example.pixel.node.dto.NodeConfiguration;
import com.example.pixel.node.entity.NodeEntity;
import com.example.pixel.node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/node")
public class NodeController {

    private final NodeService nodeService;

    @PostMapping
    public ResponseEntity<NodeEntity> create(@RequestBody NodeConfiguration nodeConfiguration) {
        return ResponseEntity.ok(nodeService.create(nodeConfiguration));
    }
}
