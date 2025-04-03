package com.example.mypixel.exception;

public class InvalidNode extends IllegalArgumentException {

    public InvalidNode(String message) {
        super(message);
    }

    public InvalidNode(String message, Throwable cause) {
        super(message, cause);
    }
}