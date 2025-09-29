package com.example.pixel.graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.entity.GraphEntity;
import com.example.pixel.graph.repository.GraphRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class GraphService {

    private static final String GRAPH_NOT_FOUND_MESSAGE = "Graph not found: ";

    private final GraphExecutor graphExecutor;
    private final GraphExecutionService graphExecutionService;
    private final GraphRepository graphRepository;

    public GraphPayload create(CreateGraphRequest createGraphRequest) {
        GraphEntity graphModel = GraphEntity
                .builder()
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .nodes(createGraphRequest.getNodes())
                .build();

        return graphRepository.save(graphModel).toPayload();
    }

    @Transactional
    public GraphPayload findById(Long id) {
        GraphEntity graphEntity = graphRepository.findById(id)
                .orElseThrow(() -> new GraphNotFoundException(GRAPH_NOT_FOUND_MESSAGE + id));

        return graphEntity.toPayload();
    }

    public GraphExecutionPayload execute(Long id) {
        GraphPayload graphPayload = findById(id);
        GraphExecutionPayload graphExecutionPayload = graphExecutionService.create(graphPayload);

        graphExecutor.startExecution(graphPayload, graphExecutionPayload);

        return graphExecutionPayload;
    }

    @Transactional
    public void updateLastAccessed(Long id) {
        if (!graphRepository.existsById(id)) {
            throw new GraphNotFoundException(GRAPH_NOT_FOUND_MESSAGE + id);
        }

        graphRepository.updateLastAccessedTime(id, LocalDateTime.now());
    }
}
