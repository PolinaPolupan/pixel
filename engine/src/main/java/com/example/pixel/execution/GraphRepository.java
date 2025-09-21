package com.example.pixel.execution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GraphRepository extends JpaRepository<GraphEntity, Long> {

    @Modifying
    @Query("UPDATE GraphEntity s SET s.lastAccessed = :now WHERE s.id = :id")
    void updateLastAccessedTime(@Param("id") Long id, @Param("now") LocalDateTime now);
}
