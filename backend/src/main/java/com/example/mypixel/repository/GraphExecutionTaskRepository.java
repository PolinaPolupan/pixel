package com.example.mypixel.repository;

import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GraphExecutionTaskRepository extends JpaRepository<GraphExecutionTask, Long> {
    Optional<GraphExecutionTask> findBySceneId(String sceneId);
    List<GraphExecutionTask> findByStatus(TaskStatus status);
}