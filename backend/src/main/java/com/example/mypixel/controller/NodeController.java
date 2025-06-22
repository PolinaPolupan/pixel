package com.example.mypixel.controller;

import com.example.mypixel.service.NodeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/node")
public class NodeController {

    @Autowired
    private NodeConfigService nodeConfigService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getNodesConfig() {
        return ResponseEntity.ok(nodeConfigService.getNodesConfig());
    }
}
