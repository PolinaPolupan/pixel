package com.example.mypixel.controller;

import com.example.mypixel.exception.*;
import com.example.mypixel.model.ErrorInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidImageFormat.class)
    public ResponseEntity<?> handleInvalidImageFormat(InvalidImageFormat ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidNodeType.class)
    public ResponseEntity<?> handleInvalidNodeType(InvalidNodeType ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidNodeParameter.class)
    public ResponseEntity<?> handleInvalidNodeParameter(InvalidNodeParameter ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageException(StorageException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
    }
}
