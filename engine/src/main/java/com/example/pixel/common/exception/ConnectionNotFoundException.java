package com.example.pixel.common.exception;

public class ConnectionNotFoundException extends IllegalArgumentException {
    public ConnectionNotFoundException(String message) {
        super(message);
    }
}