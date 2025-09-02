package com.example.pixel.exception;

public class InvalidNodeInputException extends IllegalArgumentException {

    public InvalidNodeInputException(String message) {
        super(message);
    }

    public InvalidNodeInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
