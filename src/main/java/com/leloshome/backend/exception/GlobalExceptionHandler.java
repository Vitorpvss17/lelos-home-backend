package com.leloshome.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Valor inválido para o parâmetro '" + ex.getName() + "'";
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "inválido"));

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now(),
                "status", 400,
                "error", "Validation failed",
                "fields", fieldErrors,
                "path", req.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
    }
}
