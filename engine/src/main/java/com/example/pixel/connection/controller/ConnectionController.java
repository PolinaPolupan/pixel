package com.example.pixel.connection.controller;

import com.example.pixel.connection.dto.ConnectionRequest;
import com.example.pixel.connection.entity.ConnectionEntity;
import com.example.pixel.connection.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService service;

    @PostMapping
    public ResponseEntity<ConnectionEntity> create(@RequestBody ConnectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createConnection(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectionEntity> get(@PathVariable String id) {
        return ResponseEntity.ok(service.getConnection(id));
    }
}
