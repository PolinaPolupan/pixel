package com.example.pixel.graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.entity.GraphEntity;
import com.example.pixel.graph.repository.GraphRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class GraphService {

    private final GraphExecutor graphExecutor;
    private final GraphRepository graphRepository;

    public GraphEntity createGraph(CreateGraphRequest createGraphRequest) {
        GraphEntity graphModel = GraphEntity
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .nodes(createGraphRequest.getNodes())
                .build();

        return graphRepository.save(graphModel);
    }

    public GraphExecutionPayload executeGraph(Long id) {
        GraphEntity graphEntity = graphRepository.findById(id)
                .orElseThrow(() -> new GraphNotFoundException("Graph with id: " + id + " not found"));

        return graphExecutor.startExecution(graphEntity.toPayload());
    }

    @Transactional
    public void updateLastAccessed(Long id) {
        if (!graphRepository.existsById(id)) {
            throw new GraphNotFoundException("Graph with id: " + id + " not found");
        }

        graphRepository.updateLastAccessedTime(id, LocalDateTime.now());
    }
}
