package com.example.pixel.graph_execution.service;

import com.example.pixel.common.exception.TaskNotFoundException;
import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphExecutionService {

    private final GraphExecutionRepository graphExecutionRepository;
    private final StorageService storageService;

    @Value("${dump.directory}")
    private String dumpDir;

    @Transactional
    public GraphExecutionPayload findById(Long taskId) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
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

    @Transactional
    public void updateStatus(Long id, GraphExecutionStatus status) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
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
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
        graphExecutionEntity.setProcessedNodes(processedNodes);
        graphExecutionRepository.save(graphExecutionEntity);
    }

    @Transactional
    public void markFailed(Long id, String errorMessage) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
        graphExecutionEntity.setStatus(GraphExecutionStatus.FAILED);
        if (graphExecutionEntity.getEndTime() == null) graphExecutionEntity.setEndTime(LocalDateTime.now());
        graphExecutionEntity.setErrorMessage(errorMessage);
        graphExecutionRepository.save(graphExecutionEntity);
    }

    public void delete(Long id) {
        GraphExecutionEntity graphExecutionEntity = graphExecutionRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
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