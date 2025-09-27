package com.example.pixel.node_execution.repository;

import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeExecutionRepository extends JpaRepository<NodeExecutionEntity, Long> {
}
