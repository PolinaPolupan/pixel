package com.example.mypixel.service;

import com.example.mypixel.model.Scene;
import com.example.mypixel.model.TaskStatus;
import com.example.mypixel.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SceneService {

    private final StorageService storageService;
    private final SceneRepository sceneRepository;

    public Scene createScene() {
        Scene scene = Scene
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .version(1L)
                .build();

        scene = sceneRepository.save(scene);

        String sceneId = scene.getId().toString();

        storageService.createFolder(sceneId);
        storageService.createFolder(sceneId + "/temp");
        storageService.createFolder(sceneId + "/output");
        storageService.createFolder(sceneId + "/input");

        return scene;
    }

    public void updateLastAccessed(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId).orElseThrow();
        scene.setLastAccessed(LocalDateTime.now());
        sceneRepository.save(scene);
    }

    List<Scene> getInactiveScenes() {
        return sceneRepository.findSceneIdsByStatusNotInAndLastAccessedBefore(
                List.of(TaskStatus.PENDING, TaskStatus.RUNNING), LocalDateTime.now().minusHours(1));
    }

    public void deleteScene(Long sceneId) {
        storageService.delete(sceneId.toString());
        sceneRepository.deleteById(sceneId);
    }
}
