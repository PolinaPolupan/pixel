package com.example.pixel.exception;

public class TaskNotFoundException extends IllegalArgumentException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
