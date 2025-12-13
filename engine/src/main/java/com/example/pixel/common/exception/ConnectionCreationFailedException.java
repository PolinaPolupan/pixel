package com.example.pixel.common.exception;

public class ConnectionCreationFailedException extends RuntimeException {
    public ConnectionCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}