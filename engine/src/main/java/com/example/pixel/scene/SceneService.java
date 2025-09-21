package com.example.pixel.scene;

import com.example.pixel.exception.SceneNotFoundException;
import com.example.pixel.execution.ExecutionGraphPayload;
import com.example.pixel.file_system.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class SceneService {

    private final StorageService storageService;
    private final SceneRepository sceneRepository;

    public ExecutionGraphPayload createScene() {
        Scene scene = Scene
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .version(1L)
                .build();

        scene = sceneRepository.save(scene);

        Long sceneId = scene.getId();
        ExecutionGraphPayload executionGraphPayload = new ExecutionGraphPayload(
                sceneId,
                scene.getCreatedAt(),
                scene.getLastAccessed(),
                Collections.emptyList()
        );

        storageService.createFolder("scenes/" + sceneId.toString());

        return executionGraphPayload;
    }

    @Transactional
    public void updateLastAccessed(Long sceneId) {
        if (!sceneRepository.existsById(sceneId)) {
            throw new SceneNotFoundException("Scene with id: " + sceneId + " not found");
        }

        sceneRepository.updateLastAccessedTime(sceneId, LocalDateTime.now());
    }

    public void deleteScene(Long sceneId) {
        storageService.delete("scenes/" + sceneId.toString());
        sceneRepository.deleteById(sceneId);
    }
}
