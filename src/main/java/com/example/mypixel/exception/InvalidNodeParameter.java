package com.example.mypixel.exception;

public class InvalidNodeParameter extends IllegalArgumentException {

    public InvalidNodeParameter(String message) {
        super(message);
    }

    public InvalidNodeParameter(String message, Throwable cause) {
        super(message, cause);
    }
}
