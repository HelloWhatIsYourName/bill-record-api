package com.hwiyn.billrecord.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(error(
                        exception.getStatus(),
                        exception.getCode(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_FAILED",
                        "Request validation failed",
                        request.getRequestURI(),
                        fieldErrors));
    }

    @ExceptionHandler({BadCredentialsException.class, AccessDeniedException.class})
    ResponseEntity<ApiErrorResponse> handleAccessDenied(RuntimeException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error(
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED",
                        "Authentication is required",
                        request.getRequestURI(),
                        List.of()));
    }

    private ApiErrorResponse error(
            HttpStatus status,
            String code,
            String message,
            String path,
            List<ApiErrorResponse.FieldError> fieldErrors) {
        return new ApiErrorResponse(
                Instant.now(clock),
                status.value(),
                code,
                message,
                path,
                fieldErrors);
    }
}
