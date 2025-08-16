package com.example.pixel.scene;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SceneInterceptor implements HandlerInterceptor {

    private final SceneService sceneService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();  // Example: /v1/scene/12345/graph or /v1/scene/12345/output/file

        Long sceneId = extractSceneId(path);
        if (sceneId != null) {
            sceneService.updateLastAccessed(sceneId);
        }

        return true;
    }

    private Long extractSceneId(String path) {
        // Assume path is like /v1/scene/{sceneId}/...
        String[] parts = path.split("/");
        if (parts.length >= 3 && "scene".equals(parts[2])) {
            try {
                return Long.parseLong(parts[3]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
        return null;
    }
}
