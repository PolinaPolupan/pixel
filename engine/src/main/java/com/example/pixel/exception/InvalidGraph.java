package com.example.pixel.exception;

public class InvalidGraph extends IllegalArgumentException {

    public InvalidGraph(String message) {
        super(message);
    }

    public InvalidGraph(String message, Throwable cause) {
        super(message, cause);
    }
}