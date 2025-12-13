package com.example.pixel.common.controller;

import com.example.pixel.common.model.ErrorInfo;
import com.example.pixel.common.exception.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;


@RequiredArgsConstructor
@ControllerAdvice
public class ErrorController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            StorageFileNotFoundException.class,
            GraphNotFoundException.class,
            NoSuchBucketException.class,
            GraphExecutionNotFoundException.class
    })
    public ResponseEntity<? > handleNotFound(Exception ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            InvalidFileFormat.class,
            InvalidNodeInputException. class,
            InvalidGraphException. class,
            JsonMappingException.class,
            JsonParseException.class,
            SdkClientException.class
    })
    public ResponseEntity<?> handleBadRequest(Exception ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageException(StorageException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity. status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<?> handleS3Exception(S3Exception ex, HttpServletRequest request) {
        String requestUrl = request. getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorInfo);
    }
}