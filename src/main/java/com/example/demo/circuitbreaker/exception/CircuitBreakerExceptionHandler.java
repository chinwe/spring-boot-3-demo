package com.example.demo.circuitbreaker.exception;

import com.example.demo.circuitbreaker.dto.CircuitBreakerResultDto;
import com.example.demo.circuitbreaker.dto.RateLimitExceededDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 熔断器模块异常处理器
 * 统一处理熔断器相关异常，返回友好的错误响应
 */
@RestControllerAdvice(basePackages = "com.example.demo.circuitbreaker")
public class CircuitBreakerExceptionHandler {

    /**
     * 处理熔断器打开异常
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<CircuitBreakerResultDto> handleCircuitBreakerOpen(CallNotPermittedException ex) {
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(false)
                .message("Circuit breaker is OPEN and not accepting requests. Please try again later.")
                .circuitBreakerName("circuitBreaker")
                .state("OPEN")
                .error("Service temporarily unavailable due to high failure rate")
                .timestamp(LocalDateTime.now())
                .resiliencePattern("CIRCUIT_BREAKER")
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }

    /**
     * 处理限流超出异常
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<RateLimitExceededDto> handleRateLimitExceeded(RequestNotPermitted ex) {
        RateLimitExceededDto response = RateLimitExceededDto.builder()
                .rateLimiterName("rateLimiter")
                .message("Rate limit exceeded. Too many requests.")
                .timestamp(LocalDateTime.now())
                .retryAfterSeconds(1)
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * 处理自定义限流异常
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RateLimitExceededDto> handleCustomRateLimitExceeded(RateLimitExceededException ex) {
        RateLimitExceededDto response = RateLimitExceededDto.builder()
                .caller(ex.getCaller())
                .rateLimiterName(ex.getRateLimiterName())
                .limitForPeriod(ex.getLimitForPeriod())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .retryAfterSeconds(1)
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * 处理自定义熔断器异常
     */
    @ExceptionHandler(CircuitBreakerOpenException.class)
    public ResponseEntity<CircuitBreakerResultDto> handleCustomCircuitBreakerOpen(CircuitBreakerOpenException ex) {
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(false)
                .message(ex.getMessage())
                .circuitBreakerName(ex.getCircuitBreakerName())
                .state("OPEN")
                .error("Service temporarily unavailable")
                .timestamp(LocalDateTime.now())
                .resiliencePattern("CIRCUIT_BREAKER")
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }

    /**
     * 处理舱壁已满异常
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<CircuitBreakerResultDto> handleBulkheadFull(BulkheadFullException ex) {
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error("Concurrent request limit reached. Please try again later.")
                .timestamp(LocalDateTime.now())
                .resiliencePattern("BULKHEAD")
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

    /**
     * 处理超时异常
     */
    @ExceptionHandler(TimeOutExceededException.class)
    public ResponseEntity<CircuitBreakerResultDto> handleTimeout(TimeOutExceededException ex) {
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error("Request timeout. The operation took too long to complete.")
                .timestamp(LocalDateTime.now())
                .resiliencePattern("TIMEOUT")
                .build();

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(result);
    }
}
