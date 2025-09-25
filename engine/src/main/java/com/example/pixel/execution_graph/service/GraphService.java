package com.example.pixel.execution_graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.execution_graph.model.CreateExecutionGraphRequest;
import com.example.pixel.execution_graph.model.ExecutionGraphPayload;
import com.example.pixel.execution_graph.model.GraphEntity;
import com.example.pixel.execution_graph.repository.GraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class GraphService {
    private final GraphRepository graphRepository;

    public ExecutionGraphPayload createExecutionGraph(CreateExecutionGraphRequest createExecutionGraphRequest) {
        GraphEntity graphModel = GraphEntity
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .nodes(createExecutionGraphRequest.getNodes())
                .version(1L)
                .build();

        graphModel = graphRepository.save(graphModel);

        return new ExecutionGraphPayload(
                graphModel.getId(),
                graphModel.getCreatedAt(),
                graphModel.getLastAccessed(),
                createExecutionGraphRequest.getNodes()
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
