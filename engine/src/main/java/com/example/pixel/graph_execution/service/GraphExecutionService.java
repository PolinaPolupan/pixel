package com.example.pixel.graph_execution.service;

import com.example.pixel.common.exception.GraphExecutionNotFoundException;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.mapper.GraphExecutionMapper;
import com.example.pixel.graph_execution.repository.GraphExecutionRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class GraphExecutionService {

    private static final String GRAPH_EXECUTION_NOT_FOUND_MESSAGE = "Graph execution not found: ";

    private final GraphExecutionMapper  graphExecutionMapper;
    private final GraphExecutionRepository graphExecutionRepository;

    @Transactional(readOnly = true)
    public GraphExecutionPayload findById(Long id) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        return graphExecutionMapper.toDto(graphExecutionEntity);
    }

    @Transactional
    public GraphExecutionPayload create(GraphPayload graphPayload) {
        GraphExecutionEntity graphExecutionEntity = GraphExecutionEntity
                .builder()
                .graphId(graphPayload.getId())
                .status(GraphExecutionStatus.PENDING)
                .totalNodes(graphPayload.getNodes().size())
                .processedNodes(0)
                .build();

        graphExecutionEntity = graphExecutionRepository.save(graphExecutionEntity);
        return graphExecutionMapper.toDto(graphExecutionEntity);
    }

    @Transactional
    public void updateStatus(Long id, GraphExecutionStatus status) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        graphExecutionEntity.setStatus(status);
        if (status == GraphExecutionStatus.RUNNING && graphExecutionEntity.getStartTime() == null) {
            graphExecutionEntity.setStartTime(LocalDateTime.now());
        } else if ((status == GraphExecutionStatus.COMPLETED || status == GraphExecutionStatus.FAILED)
                && graphExecutionEntity.getEndTime() == null) {
            graphExecutionEntity.setEndTime(LocalDateTime.now());
        }
        graphExecutionRepository.save(graphExecutionEntity);
    }

    @Transactional
    public void updateProgress(Long id, int processedNodes) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        graphExecutionEntity.setProcessedNodes(processedNodes);
        graphExecutionRepository.save(graphExecutionEntity);
    }

    @Transactional
    public void markFailed(Long id, String errorMessage) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        graphExecutionEntity.setStatus(GraphExecutionStatus.FAILED);
        if (graphExecutionEntity.getEndTime() == null) graphExecutionEntity.setEndTime(LocalDateTime.now());
        graphExecutionEntity.setErrorMessage(errorMessage);
        graphExecutionRepository.save(graphExecutionEntity);
    }

    @Transactional
    public void delete(Long id) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        graphExecutionRepository.delete(graphExecutionEntity);
    }

    @Transactional(readOnly = true)
    public List<GraphExecutionPayload> getInactive() {
        List<GraphExecutionEntity> inactiveExecutionEntities = graphExecutionRepository.findByStatusNotIn(List.of(GraphExecutionStatus.PENDING, GraphExecutionStatus.RUNNING));
        List<GraphExecutionPayload> inactiveExecutions = new ArrayList<>();

        for (GraphExecutionEntity graphExecutionEntity : inactiveExecutionEntities) {
            GraphExecutionPayload graphExecutionPayload = graphExecutionMapper.toDto(graphExecutionEntity);
            inactiveExecutions.add(graphExecutionPayload);
        }

        return inactiveExecutions;
    }
}