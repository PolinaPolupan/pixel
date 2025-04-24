package com.example.mypixel.controller;

import com.example.mypixel.model.Scene;
import com.example.mypixel.repository.SceneRepository;
import com.example.mypixel.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/v1/scene")
public class SceneController {

    private final StorageService storageService;
    private final SceneRepository sceneRepository;

    @Autowired
    public SceneController(StorageService storageService, SceneRepository sceneRepository) {
        this.storageService = storageService;
        this.sceneRepository = sceneRepository;
    }

    @PostMapping("/")
    public ResponseEntity<Scene> createScene() {
        Scene scene = new Scene();

        scene.setCreatedAt(LocalDateTime.now());
        scene.setVersion(1L);
        scene = sceneRepository.save(scene);

        String sceneId = scene.getId().toString();

        storageService.createFolder(sceneId);
        storageService.createFolder(sceneId + "/temp");
        storageService.createFolder(sceneId + "/output");
        storageService.createFolder(sceneId + "/input");

        return ResponseEntity.status(HttpStatus.CREATED).body(scene);
    }
}
