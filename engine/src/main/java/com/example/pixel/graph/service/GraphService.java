package com.example.pixel.graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.entity.GraphEntity;
import com.example.pixel.graph.mapper.GraphMapper;
import com.example.pixel.graph.repository.GraphRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GraphService {

    private static final String GRAPH_NOT_FOUND_MESSAGE = "Graph not found: ";

    @Value("${default.schedule}")
    private String defaultSchedule;

    private final GraphMapper graphMapper;
    private final GraphExecutor graphExecutor;
    private final GraphExecutionService graphExecutionService;
    private final GraphRepository graphRepository;

    @Transactional
    public GraphPayload create(CreateGraphRequest createGraphRequest) {
        if (graphRepository.existsById(createGraphRequest.getId())) {
            throw new IllegalArgumentException("Graph with id " + createGraphRequest.getId() + " already exists.");
        }

        GraphEntity graphModel = GraphEntity.builder()
                .id(createGraphRequest.getId())
                .createdAt(LocalDateTime.now())
                .nodes(createGraphRequest.getNodes())
                .schedule(
                        createGraphRequest.getSchedule() != null
                                ? createGraphRequest.getSchedule()
                                : defaultSchedule
                )
                .build();

        return graphMapper.toDto(graphRepository.save(graphModel));
    }


    @Transactional(readOnly = true)
    public GraphPayload findById(String id) {
        GraphEntity graphEntity = graphRepository.findById(id)
                .orElseThrow(() -> new GraphNotFoundException(GRAPH_NOT_FOUND_MESSAGE + id));

        return graphMapper.toDto(graphEntity);
    }

    @Transactional(readOnly = true)
    public List<GraphPayload> findAll() {
        List<GraphEntity> graphEntities = graphRepository.findAll();
        return graphEntities.stream()
                .map(graphMapper::toDto)
                .toList();
    }

    public GraphExecutionPayload execute(GraphPayload graphPayload) {
        GraphExecutionPayload graphExecutionPayload = graphExecutionService.create(graphPayload);

        graphExecutor.startExecution(graphPayload, graphExecutionPayload);

        return graphExecutionPayload;
    }
}
