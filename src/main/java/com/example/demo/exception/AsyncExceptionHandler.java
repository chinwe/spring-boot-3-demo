package com.example.demo.exception;

import com.example.demo.dto.AsyncErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

/**
 * 异步操作异常处理器
 * 优先级设置为 2，在特定业务异常处理器之后执行
 *
 * @author chinwe
 */
@Slf4j
@Order(2)
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

    /**
     * 处理异步操作中的 RuntimeException
     * 排除 EntityNotFoundException，由 JooqExceptionHandler 处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AsyncErrorResponse> handleRuntimeException(RuntimeException ex) {
        // 让 JooqExceptionHandler 处理 EntityNotFoundException
        if (ex instanceof JooqExceptionHandler.EntityNotFoundException) {
            throw ex;
        }

        log.error("Runtime exception in async operation", ex);

        AsyncErrorResponse errorResponse = AsyncErrorResponse.builder()
                .taskId("runtime-error-" + System.currentTimeMillis())
                .errorCode("RUNTIME_ERROR")
                .errorMessage("Async operation failed: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}