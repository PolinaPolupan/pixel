package com.example.pixel.execution;

import com.example.pixel.exception.SceneNotFoundException;
import com.example.pixel.file_system.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class GraphService {

    private final StorageService storageService;
    private final GraphRepository graphRepository;

    public ExecutionGraphPayload createExecutionGraph() {
        GraphEntity graphModel = GraphEntity
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .version(1L)
                .build();

        graphModel = graphRepository.save(graphModel);

        Long sceneId = graphModel.getId();
        ExecutionGraphPayload executionGraphPayload = new ExecutionGraphPayload(
                sceneId,
                graphModel.getCreatedAt(),
                graphModel.getLastAccessed(),
                Collections.emptyList()
        );

        storageService.createFolder("scenes/" + sceneId.toString());

        return executionGraphPayload;
    }

    @Transactional
    public void updateLastAccessed(Long id) {
        if (!graphRepository.existsById(id)) {
            throw new SceneNotFoundException("Graph with id: " + id + " not found");
        }

        graphRepository.updateLastAccessedTime(id, LocalDateTime.now());
    }

    public void deleteGraph(Long id) {
        storageService.delete("scenes/" + id.toString());
        graphRepository.deleteById(id);
    }
}
