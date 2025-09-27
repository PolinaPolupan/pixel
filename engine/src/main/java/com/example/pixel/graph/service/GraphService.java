package com.example.pixel.graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.model.Graph;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.entity.GraphEntity;
import com.example.pixel.graph.repository.GraphRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class GraphService {

    private final GraphExecutor graphExecutor;
    private final GraphRepository graphRepository;

    public GraphPayload createGraph(CreateGraphRequest createGraphRequest) {
        GraphEntity graphModel = GraphEntity
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .nodes(createGraphRequest.getNodes())
                .build();

        graphModel = graphRepository.save(graphModel);

        return new GraphPayload(
                graphModel.getId(),
                graphModel.getCreatedAt(),
                graphModel.getLastAccessed(),
                createGraphRequest.getNodes()
        );
    }

    public GraphExecutionPayload executeGraph(Long id) {
        GraphEntity graphEntity = graphRepository.findById(id)
                .orElseThrow(() -> new GraphNotFoundException("Graph with id: " + id + " not found"));

        Graph graph = graphEntity.toGraph();
        return graphExecutor.startExecution(graph);
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
