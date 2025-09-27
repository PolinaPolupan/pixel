package com.example.pixel.execution_graph.service;

import com.example.pixel.common.exception.GraphNotFoundException;
import com.example.pixel.execution_graph.dto.CreateExecutionGraphRequest;
import com.example.pixel.execution_graph.model.ExecutionGraph;
import com.example.pixel.execution_graph.dto.ExecutionGraphPayload;
import com.example.pixel.execution_graph.entity.GraphEntity;
import com.example.pixel.execution_graph.repository.GraphRepository;
import com.example.pixel.execution_task.dto.ExecutionTaskPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class GraphService {

    private final GraphExecutor graphExecutor;
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

    public ExecutionTaskPayload executeGraph(Long id) {
        GraphEntity graphEntity = graphRepository.findById(id)
                .orElseThrow(() -> new GraphNotFoundException("Graph with id: " + id + " not found"));

        ExecutionGraph executionGraph = graphEntity.toExecutionGraph();
        return graphExecutor.startExecution(executionGraph);
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
