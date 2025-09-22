package com.example.pixel.execution;

import com.example.pixel.exception.GraphNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class GraphService {
    private final GraphRepository graphRepository;

    public ExecutionGraphPayload createExecutionGraph() {
        GraphEntity graphModel = GraphEntity
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .version(1L)
                .build();

        graphModel = graphRepository.save(graphModel);

        return new ExecutionGraphPayload(
                graphModel.getId(),
                graphModel.getCreatedAt(),
                graphModel.getLastAccessed(),
                Collections.emptyList()
        );
    }

    @Transactional
    public void updateLastAccessed(Long id) {
        if (!graphRepository.existsById(id)) {
            throw new GraphNotFoundException("Graph with id: " + id + " not found");
        }

        graphRepository.updateLastAccessedTime(id, LocalDateTime.now());
    }

    public void deleteGraph(Long id) {
        graphRepository.deleteById(id);
    }
}
