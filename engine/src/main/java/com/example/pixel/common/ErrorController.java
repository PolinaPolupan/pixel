package com.example.pixel.common;

import com.example.pixel.exception.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
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

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ControllerAdvice
public class ErrorController {
    private final MeterRegistry meterRegistry;

    private void recordExceptionMetric(Exception ex, HttpServletRequest request, HttpStatus status) {
        String exceptionName = ex.getClass().getSimpleName();
        String path = request.getRequestURI();
        String method = request.getMethod();

        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("exception", exceptionName));
        tags.add(Tag.of("path", path));
        tags.add(Tag.of("method", method));
        tags.add(Tag.of("status", String.valueOf(status.value())));

        Counter.builder("application.exceptions")
                .description("Count of exceptions handled by the application")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.NOT_FOUND);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SceneNotFoundException.class)
    public ResponseEntity<?> handleSceneNotFound(SceneNotFoundException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.NOT_FOUND);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidImageFormat.class)
    public ResponseEntity<?> handleInvalidImageFormat(InvalidImageFormat ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidNode.class)
    public ResponseEntity<?> handleInvalidNodeType(InvalidNode ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidNodeParameter.class)
    public ResponseEntity<?> handleInvalidNodeParameter(InvalidNodeParameter ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidGraph.class)
    public ResponseEntity<?> handleInvalidGraph(InvalidGraph ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageException(StorageException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<?> handleJsonException(JsonMappingException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<?> handleJsonException(JsonParseException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<?> handleS3Exception(S3Exception ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.FORBIDDEN);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchBucketException.class)
    public ResponseEntity<?> handleNoBucketException(NoSuchBucketException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.NOT_FOUND);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<?> handleSdkClientException(SdkClientException ex, HttpServletRequest request) {
        recordExceptionMetric(ex, request, HttpStatus.BAD_REQUEST);
        String requestUrl = request.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(requestUrl, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }
}
