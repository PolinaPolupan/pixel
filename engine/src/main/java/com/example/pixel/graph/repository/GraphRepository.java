package com.example.pixel.graph.repository;

import com.example.pixel.graph.entity.GraphEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GraphRepository extends JpaRepository<GraphEntity, Long> {
}
