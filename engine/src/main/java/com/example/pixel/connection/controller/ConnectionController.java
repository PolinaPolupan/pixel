package com.example.pixel.connection.controller;

import com.example.pixel.connection.dto.ConnectionDto;
import com.example.pixel.connection.dto.ConnectionRequest;
import com.example.pixel.connection.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService service;

    @PostMapping
    public ResponseEntity<ConnectionDto> create(@RequestBody ConnectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectionDto> get(@PathVariable String id) {
        return ResponseEntity.ok(service.findByConnId(id));
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
