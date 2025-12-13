package com.example.pixel.graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph.entity.GraphEntity;
import com.example.pixel.graph.mapper.GraphMapper;
import com.example.pixel.graph.repository.GraphRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionDto;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import com.example.pixel.graph_execution.service.GraphExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GraphService {

    private static final String GRAPH_NOT_FOUND_MESSAGE = "Graph not found: ";

    private final GraphMapper graphMapper;
    private final GraphExecutor graphExecutor;
    private final GraphExecutionService graphExecutionService;
    private final GraphRepository graphRepository;
    private final GraphValidator graphValidator;

    @Transactional
    public GraphDto create(CreateGraphRequest createGraphRequest) {
        if (graphRepository.existsByGraphId(createGraphRequest.getId())) {
            log.warn("Graph already exists with id: {}", createGraphRequest.getId());
            return findById(createGraphRequest.getId());
        }

        graphValidator.validateGraphIntegrity(createGraphRequest);

        GraphEntity graphModel = GraphEntity.builder()
                .graphId(createGraphRequest.getId())
                .createdAt(LocalDateTime.now())
                .nodes(createGraphRequest.getNodes())
                .schedule(createGraphRequest.getSchedule())
                .build();

        return graphMapper.toDto(graphRepository.save(graphModel));
    }

    @Transactional(readOnly = true)
    public GraphDto findById(String id) {
        GraphEntity graphEntity = graphRepository.findByGraphId(id)
                .orElseThrow(() -> new GraphNotFoundException(GRAPH_NOT_FOUND_MESSAGE + id));

        return graphMapper.toDto(graphEntity);
    }

    @Transactional(readOnly = true)
    public List<GraphDto> findAll() {
        List<GraphEntity> graphEntities = graphRepository.findAll();
        return graphEntities.stream()
                .map(graphMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteById(String id) {
        graphRepository.deleteByGraphId(id);
    }

    public GraphExecutionDto execute(GraphDto graphDto) {
        GraphExecutionDto graphExecutionDto = graphExecutionService.create(graphDto);

        graphExecutor.launchExecution(graphDto, graphExecutionDto);

        return graphExecutionDto;
    }
}
