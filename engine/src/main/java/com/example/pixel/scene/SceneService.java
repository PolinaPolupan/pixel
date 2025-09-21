package com.example.pixel.scene;

import com.example.pixel.exception.SceneNotFoundException;
import com.example.pixel.execution.ExecutionGraphPayload;
import com.example.pixel.execution.ExecutionGraphRepository;
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
    private final ExecutionGraphRepository executionGraphRepository;

    public ExecutionGraphPayload createExecutionGraph() {
        Scene scene = Scene
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .version(1L)
                .build();

        scene = executionGraphRepository.save(scene);

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
    public void updateLastAccessed(Long id) {
        if (!executionGraphRepository.existsById(id)) {
            throw new SceneNotFoundException("Graph with id: " + id + " not found");
        }

        executionGraphRepository.updateLastAccessedTime(id, LocalDateTime.now());
    }

    public void deleteGraph(Long id) {
        storageService.delete("scenes/" + id.toString());
        executionGraphRepository.deleteById(id);
    }
}
