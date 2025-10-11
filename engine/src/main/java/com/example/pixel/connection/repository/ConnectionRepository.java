package com.example.pixel.connection.repository;

import com.example.pixel.connection.entity.ConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Long> {
    Optional<ConnectionEntity> findByConnId(String connId);
}
