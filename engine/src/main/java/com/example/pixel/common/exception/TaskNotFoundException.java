package com.example.pixel.common.exception;

public class TaskNotFoundException extends IllegalArgumentException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
