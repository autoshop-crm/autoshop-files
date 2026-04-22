package com.vladko.autoshopfilestorage.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidFile(InvalidFileException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "FILE_VALIDATION_FAILED", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(FileNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(FileNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(DeletedFileException.class)
    ResponseEntity<ApiErrorResponse> handleDeleted(DeletedFileException exception, HttpServletRequest request) {
        return build(HttpStatus.GONE, "FILE_DELETED", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(FileNotAvailableException.class)
    ResponseEntity<ApiErrorResponse> handleNotAvailable(FileNotAvailableException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "FILE_NOT_AVAILABLE", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(StorageUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleStorageUnavailable(StorageUnavailableException exception, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            HttpMediaTypeNotSupportedException.class
    })
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ApiErrorResponse> handleBinding(Exception exception, HttpServletRequest request) {
        List<ApiErrorResponse.FieldViolation> details;
        if (exception instanceof MethodArgumentNotValidException ex) {
            details = ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ApiErrorResponse.FieldViolation(error.getField(), error.getDefaultMessage()))
                    .toList();
        } else if (exception instanceof BindException ex) {
            details = ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ApiErrorResponse.FieldViolation(error.getField(), error.getDefaultMessage()))
                    .toList();
        } else {
            details = List.of();
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request, details);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Uploaded file exceeds service limit", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ApiErrorResponse.FieldViolation> details
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
