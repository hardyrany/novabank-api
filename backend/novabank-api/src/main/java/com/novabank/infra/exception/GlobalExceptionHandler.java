package com.novabank.infra.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException businessException, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().message(businessException.getMessage())
                .status(HttpStatus.BAD_REQUEST.value()).error("Business Error")
                .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException resourceNotFoundException, WebRequest request) {
        ErrorResponse response =
                ErrorResponse.builder().message(resourceNotFoundException.getMessage())
                        .status(HttpStatus.NOT_FOUND.value()).error("Resource Not Found")
                        .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException unauthorizedException, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().message(unauthorizedException.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value()).error("Unauthorized")
                .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException forbiddenException, WebRequest request) {

        ErrorResponse errorResponse =
                ErrorResponse.builder().message(forbiddenException.getMessage())
                        .status(HttpStatus.FORBIDDEN.value()).error("Forbidden")
                        .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException conflictException, WebRequest request) {

        ErrorResponse errorResponse =
                ErrorResponse.builder().message(conflictException.getMessage())
                        .status(HttpStatus.CONFLICT.value()).error("Conflict")
                        .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException validationException, WebRequest request) {

        String message = validationException.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse response = ErrorResponse.builder().message(message)
                .status(HttpStatus.BAD_REQUEST.value()).error("Validation Error")
                .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception, WebRequest request) {

        ErrorResponse response = ErrorResponse.builder().message("Access denied")
                .status(HttpStatus.FORBIDDEN.value()).error("Forbidden")
                .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception,
            WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error("Internal Server Error")
                .path(request.getDescription(false)).timestamp(LocalDateTime.now()).build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
