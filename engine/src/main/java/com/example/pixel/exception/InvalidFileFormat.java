package com.example.pixel.exception;

public class InvalidFileFormat extends IllegalArgumentException {

    public InvalidFileFormat(String message) {
        super(message);
    }

    public InvalidFileFormat(String message, Throwable cause) {
        super(message, cause);
    }
}
