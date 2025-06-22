package com.example.mypixel.controller;

import com.example.mypixel.model.Scene;
import com.example.mypixel.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/scene")
public class SceneController {

   private final SceneService sceneService;

    @PostMapping("/")
    public ResponseEntity<Scene> createScene() {
        return ResponseEntity.status(HttpStatus.CREATED).body(sceneService.createScene());
    }
}
