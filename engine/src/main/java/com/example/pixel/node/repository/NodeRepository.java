package com.example.pixel.node.repository;

import com.example.pixel.node.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<NodeEntity, Long> {
    @Query("SELECT n FROM NodeEntity n WHERE n.type = :type ORDER BY n.version DESC")
    Optional<NodeEntity> findLatestByType(String type);
}
