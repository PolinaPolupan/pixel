package com.example.pixel.node.controller;

import com.example.pixel.node.dto.NodeConfigurationRequest;
import com.example.pixel.node.dto.NodeConfigurationDto;
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
    public ResponseEntity<NodeConfigurationDto> create(@RequestBody NodeConfigurationRequest nodeConfigurationRequest) {
        return ResponseEntity.ok(nodeService.create(nodeConfigurationRequest));
    }

    @GetMapping
    public ResponseEntity<Map<String, NodeConfigurationDto>> getAllNodes() {
        return ResponseEntity.ok(nodeService.getAllActiveNodes());
    }
}
