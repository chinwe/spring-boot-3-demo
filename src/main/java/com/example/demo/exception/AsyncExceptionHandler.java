package com.example.demo.exception;

import com.example.demo.dto.AsyncErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestControllerAdvice
public class AsyncExceptionHandler {

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<AsyncErrorResponse> handleAsyncTimeout(AsyncRequestTimeoutException ex) {
        log.error("Async request timeout occurred", ex);
        
        AsyncErrorResponse errorResponse = AsyncErrorResponse.builder()
                .taskId("timeout-" + System.currentTimeMillis())
                .errorCode("ASYNC_TIMEOUT")
                .errorMessage("Async request timed out")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<AsyncErrorResponse> handleTimeout(TimeoutException ex) {
        log.error("Timeout exception occurred", ex);
        
        AsyncErrorResponse errorResponse = AsyncErrorResponse.builder()
                .taskId("timeout-" + System.currentTimeMillis())
                .errorCode("OPERATION_TIMEOUT")
                .errorMessage("Operation timed out: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AsyncErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error in async request", ex);
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed");
        
        AsyncErrorResponse errorResponse = AsyncErrorResponse.builder()
                .taskId("validation-error-" + System.currentTimeMillis())
                .errorCode("VALIDATION_ERROR")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AsyncErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception in async operation", ex);
        
        AsyncErrorResponse errorResponse = AsyncErrorResponse.builder()
                .taskId("runtime-error-" + System.currentTimeMillis())
                .errorCode("RUNTIME_ERROR")
                .errorMessage("Async operation failed: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AsyncErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception in async operation", ex);
        
        AsyncErrorResponse errorResponse = AsyncErrorResponse.builder()
                .taskId("generic-error-" + System.currentTimeMillis())
                .errorCode("INTERNAL_ERROR")
                .errorMessage("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}