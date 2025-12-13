package com.example.pixel.graph_execution.repository;

import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import com.example.pixel.graph_execution.dto.GraphExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GraphExecutionRepository extends JpaRepository<GraphExecutionEntity, Long> {
    List<GraphExecutionEntity> findByStatusNotIn(List<GraphExecutionStatus> statuses);
    List<GraphExecutionEntity> findByGraphId(String graphId);
}