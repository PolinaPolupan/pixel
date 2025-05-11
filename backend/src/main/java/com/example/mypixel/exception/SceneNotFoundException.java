package com.example.mypixel.exception;

public class SceneNotFoundException extends RuntimeException {
    public SceneNotFoundException(String message) {
        super(message);
    }
}
