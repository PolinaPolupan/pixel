package com.example.pixel.graph_execution.repository;

import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GraphExecutionRepository extends JpaRepository<GraphExecutionEntity, Long> {
    List<GraphExecutionEntity> findByGraphId(String graphId);
    List<GraphExecutionEntity> findByEndTimeBefore(LocalDateTime dateTime);
}