package com.example.pixel.execution_task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionTaskRepository extends JpaRepository<ExecutionTaskEntity, Long> {
    List<ExecutionTaskEntity> findByStatusNotIn(List<ExecutionTaskStatus> statuses);
}