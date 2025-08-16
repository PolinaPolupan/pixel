package com.example.pixel.exception;

public class SceneNotFoundException extends RuntimeException {
    public SceneNotFoundException(String message) {
        super(message);
    }
}
