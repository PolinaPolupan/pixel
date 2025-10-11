package com.example.pixel.graph.repository;

import com.example.pixel.graph.entity.GraphEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface GraphRepository extends JpaRepository<GraphEntity, String> {
    boolean existsByGraphId(String graphId);
    Optional<GraphEntity> findByGraphId(String graphId);
    void deleteByGraphId(String graphId);
}
