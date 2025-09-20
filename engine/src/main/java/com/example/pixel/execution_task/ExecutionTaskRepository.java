package com.example.pixel.execution_task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionTaskRepository extends JpaRepository<ExecutionTask, Long> {
    List<ExecutionTask> findByStatusNotIn(List<ExecutionTaskStatus> statuses);
}