package com.example.pixel.scene;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ScenePayload {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;

    public static ScenePayload fromEntity(Scene scene) {
        if (scene == null) return null;
        return new ScenePayload(scene.getId(), scene.getCreatedAt(), scene.getLastAccessed());
    }
}
