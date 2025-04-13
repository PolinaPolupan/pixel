package com.example.mypixel.controller;

import com.example.mypixel.model.Scene;
import com.example.mypixel.service.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/scene")
public class SceneController {

    private final FileManager fileManager;

    @Autowired
    public SceneController(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @PostMapping("/")
    public ResponseEntity<Scene> createScene() {
        UUID uuid = UUID.randomUUID();
        fileManager.createScene(uuid.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(new Scene(uuid.toString()));
    }
}
