package com.example.mypixel.exception;

public class InvalidNodeType extends IllegalArgumentException {

    public InvalidNodeType(String message) {
        super(message);
    }

    public InvalidNodeType(String message, Throwable cause) {
        super(message, cause);
    }
}
