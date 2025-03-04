package com.example.mypixel.exception;

public class InvalidImageFormat extends IllegalArgumentException {

    public InvalidImageFormat(String message) {
        super(message);
    }

    public InvalidImageFormat(String message, Throwable cause) {
        super(message, cause);
    }
}
