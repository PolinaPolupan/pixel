package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Vector2D <T> {
    private T x;
    private T y;

    public static <T> Vector2D<T> fromMap(Map<String, Object> map) {
        T x = (T) map.get("x");
        T y = (T) map.get("y");
        return new Vector2D<>(x, y);
    }
}
