package com.example.pixel.execution_task.repository;

import com.example.pixel.execution_task.entity.ExecutionTaskEntity;
import com.example.pixel.execution_task.dto.ExecutionTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionTaskRepository extends JpaRepository<ExecutionTaskEntity, Long> {
    List<ExecutionTaskEntity> findByStatusNotIn(List<ExecutionTaskStatus> statuses);
}