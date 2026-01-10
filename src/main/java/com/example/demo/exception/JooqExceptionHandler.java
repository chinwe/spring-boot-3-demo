package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JOOQ 模块异常处理器
 * 处理 JOOQ 相关的业务异常
 * 优先级设置为 1，确保在通用异常处理器之前执行
 *
 * @author chinwe
 */
@Slf4j
@Order(1)
@RestControllerAdvice
public class JooqExceptionHandler {

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(
            Map.of(
                "errorCode", "INVALID_ARGUMENT",
                "errorMessage", ex.getMessage(),
                "timestamp", LocalDateTime.now()
            )
        );
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of(
                "errorCode", "OPERATION_FAILED",
                "errorMessage", ex.getMessage(),
                "timestamp", LocalDateTime.now()
            )
        );
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Map.of(
                "errorCode", "NOT_FOUND",
                "errorMessage", ex.getMessage(),
                "timestamp", LocalDateTime.now()
            )
        );
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing + "; " + replacement
            ));

        return ResponseEntity.badRequest().body(
            Map.of(
                "errorCode", "VALIDATION_FAILED",
                "errorMessage", "Input validation failed",
                "fieldErrors", fieldErrors,
                "timestamp", LocalDateTime.now()
            )
        );
    }

    /**
     * 处理所有未捕获的异常（最后兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of(
                "errorCode", "INTERNAL_ERROR",
                "errorMessage", "An unexpected error occurred. Please try again later.",
                "timestamp", LocalDateTime.now(),
                "referenceId", UUID.randomUUID().toString()
            )
        );
    }

    /**
     * 资源不存在自定义异常
     */
    public static class EntityNotFoundException extends RuntimeException {
        public EntityNotFoundException(String message) {
            super(message);
        }
    }
}
