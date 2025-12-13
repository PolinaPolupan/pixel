package com.example.pixel.node.repository;

import com.example.pixel.node.entity.NodeConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<NodeConfigurationEntity, Long> {
    @Query("SELECT n FROM NodeConfigurationEntity n WHERE n.type = :type ORDER BY n.version DESC LIMIT 1")
    Optional<NodeConfigurationEntity> findLatestByType(String type);
    List<NodeConfigurationEntity> findByActiveTrue();
}
