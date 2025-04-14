package com.example.mypixel.controller;

import com.example.mypixel.model.Scene;
import com.example.mypixel.service.StorageService;
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

    private final StorageService storageService;

    @Autowired
    public SceneController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/")
    public ResponseEntity<Scene> createScene() {
        UUID uuid = UUID.randomUUID();

        String sceneId = uuid.toString();

        storageService.createFolder(sceneId);
        storageService.createFolder(sceneId + "/temp");
        storageService.createFolder(sceneId + "/output");
        storageService.createFolder(sceneId + "/input");

        return ResponseEntity.status(HttpStatus.CREATED).body(new Scene(uuid.toString()));
    }
}
