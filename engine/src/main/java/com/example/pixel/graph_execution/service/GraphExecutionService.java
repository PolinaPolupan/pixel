package com.example.pixel.graph_execution.service;

import com.example.pixel.common.exception.GraphExecutionNotFoundException;
import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.entity.GraphEntity;
import com.example.pixel.graph.repository.GraphRepository;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.executor.GraphExecutor;
import com.example.pixel.graph_execution.repository.GraphExecutionRepository;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import com.example.pixel.file_system.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class GraphExecutionService {

    private static final String GRAPH_EXECUTION_NOT_FOUND_MESSAGE = "Graph execution not found: ";

    private final GraphExecutor graphExecutor;
    private final GraphRepository graphRepository;
    private final GraphExecutionRepository graphExecutionRepository;
    private final StorageService storageService;

    @Value("${dump.directory}")
    private String dumpDir;

    @Transactional
    public GraphExecutionPayload findById(Long id) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        return GraphExecutionPayload.fromEntity(graphExecutionEntity);
    }

    @Transactional
    public GraphExecutionEntity create(GraphPayload graph) {
        GraphExecutionEntity graphExecutionEntity = GraphExecutionEntity
                .builder()
                .graphId(graph.getId())
                .status(GraphExecutionStatus.PENDING)
                .totalNodes(graph.getNodeExecutions().size())
                .processedNodes(0)
                .build();
        return graphExecutionRepository.save(graphExecutionEntity);
    }

    public GraphExecutionPayload executeGraph(Long id) {
        GraphEntity graphEntity = graphRepository.findById(id)
                .orElseThrow(() -> new GraphNotFoundException("Graph with id: " + id + " not found"));

        return graphExecutor.startExecution(graphEntity.toPayload());
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

    public void delete(Long id) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new GraphExecutionNotFoundException(GRAPH_EXECUTION_NOT_FOUND_MESSAGE + id));
        storageService.delete(dumpDir + "/" + id);
        graphExecutionRepository.delete(graphExecutionEntity);
    }

    public List<GraphExecutionPayload> getInactive() {
        List<GraphExecutionEntity> inactiveExecutionTaskEntities = graphExecutionRepository.findByStatusNotIn(List.of(GraphExecutionStatus.PENDING, GraphExecutionStatus.RUNNING));

        return inactiveExecutionTaskEntities.stream()
                .map(GraphExecutionPayload::fromEntity)
                .collect(Collectors.toList());
    }
}