package com.example.mypixel.repository;

import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GraphExecutionTaskRepository extends JpaRepository<GraphExecutionTask, Long> {
    List<GraphExecutionTask> findByStatusNotIn(List<TaskStatus> statuses);
}