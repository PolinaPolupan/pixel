package com.example.pixel.execution;

import com.example.pixel.scene.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ExecutionGraphRepository extends JpaRepository<Scene, Long> {

    @Modifying
    @Query("UPDATE Scene s SET s.lastAccessed = :now WHERE s.id = :sceneId")
    void updateLastAccessedTime(@Param("sceneId") Long sceneId, @Param("now") LocalDateTime now);
}
