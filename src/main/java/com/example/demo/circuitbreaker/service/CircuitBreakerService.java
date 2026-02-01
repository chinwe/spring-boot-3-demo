package com.example.demo.circuitbreaker.service;

import com.example.demo.circuitbreaker.dto.CircuitBreakerResultDto;
import com.example.demo.circuitbreaker.dto.ExternalApiRequestDto;
import com.example.demo.circuitbreaker.model.CallerRateLimit;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 熔断器服务类
 * 使用配置文件方式实现容错模式
 */
@Service
@Slf4j
public class CircuitBreakerService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ExternalApiService externalApiService;

    public CircuitBreakerService(
            CircuitBreakerRegistry circuitBreakerRegistry,
            ExternalApiService externalApiService) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.externalApiService = externalApiService;
    }

    /**
     * 使用熔断器调用外部 API
     * 配置: externalApi
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(
            name = "externalApi",
            fallbackMethod = "circuitBreakerFallback"
    )
    public CircuitBreakerResultDto callExternalApiWithCircuitBreaker(ExternalApiRequestDto request) {
        long startTime = System.currentTimeMillis();
        String result = externalApiService.callExternalApi(request);

        return CircuitBreakerResultDto.builder()
                .success(true)
                .message(result)
                .circuitBreakerName("externalApi")
                .state(getCircuitBreakerState("externalApi"))
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .resiliencePattern("CIRCUIT_BREAKER")
                .build();
    }

    /**
     * 使用限流器调用 API
     * 配置: apiRateLimiter
     */
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(
            name = "apiRateLimiter",
            fallbackMethod = "rateLimiterFallback"
    )
    public CircuitBreakerResultDto callApiWithRateLimit(String endpoint) {
        long startTime = System.currentTimeMillis();
        String result = externalApiService.callFastApi(endpoint);

        return CircuitBreakerResultDto.builder()
                .success(true)
                .message(result)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .resiliencePattern("RATE_LIMITER")
                .build();
    }

    /**
     * 使用舱壁隔离调用 API
     * 配置: apiBulkhead
     */
    @io.github.resilience4j.bulkhead.annotation.Bulkhead(
            name = "apiBulkhead",
            fallbackMethod = "bulkheadFallback"
    )
    public CircuitBreakerResultDto callApiWithBulkhead(String endpoint) {
        long startTime = System.currentTimeMillis();
        String result = externalApiService.callFastApi(endpoint);

        return CircuitBreakerResultDto.builder()
                .success(true)
                .message(result)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .resiliencePattern("BULKHEAD")
                .build();
    }

    /**
     * 使用超时控制调用 API
     * 配置: apiTimeLimiter
     */
    @io.github.resilience4j.timelimiter.annotation.TimeLimiter(
            name = "apiTimeLimiter",
            fallbackMethod = "timeoutFallback"
    )
    public java.util.concurrent.CompletableFuture<String> callApiWithTimeout(String endpoint, long delayMs) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() ->
                externalApiService.callSlowApi(endpoint, delayMs)
        );
    }

    /**
     * 组合使用所有容错模式调用 API
     * 配置: combinedCircuitBreaker, combinedRateLimiter, combinedBulkhead
     */
    @io.github.resilience4j.bulkhead.annotation.Bulkhead(name = "combinedBulkhead")
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "combinedRateLimiter")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(
            name = "combinedCircuitBreaker",
            fallbackMethod = "combinedFallback"
    )
    public CircuitBreakerResultDto callApiWithAllResiliencePatterns(ExternalApiRequestDto request) {
        long startTime = System.currentTimeMillis();
        String result = externalApiService.callExternalApi(request);

        return CircuitBreakerResultDto.builder()
                .success(true)
                .message(result)
                .circuitBreakerName("combinedCircuitBreaker")
                .state(getCircuitBreakerState("combinedCircuitBreaker"))
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .resiliencePattern("COMBINED")
                .build();
    }

    // ==================== Fallback 方法 ====================

    private CircuitBreakerResultDto circuitBreakerFallback(ExternalApiRequestDto request, Exception ex) {
        log.warn("Circuit breaker fallback triggered for endpoint: {}", request.getEndpoint(), ex);
        return CircuitBreakerResultDto.builder()
                .success(false)
                .message("Circuit breaker fallback: " + ex.getMessage())
                .circuitBreakerName("externalApi")
                .state(getCircuitBreakerState("externalApi"))
                .error(ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .resiliencePattern("CIRCUIT_BREAKER")
                .build();
    }

    private CircuitBreakerResultDto rateLimiterFallback(String endpoint, Exception ex) {
        log.warn("Rate limiter fallback triggered for endpoint: {}", endpoint, ex);
        return CircuitBreakerResultDto.builder()
                .success(false)
                .message("Rate limit exceeded: " + ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .resiliencePattern("RATE_LIMITER")
                .build();
    }

    private CircuitBreakerResultDto bulkheadFallback(String endpoint, Exception ex) {
        log.warn("Bulkhead fallback triggered for endpoint: {}", endpoint, ex);
        return CircuitBreakerResultDto.builder()
                .success(false)
                .message("Bulkhead full: " + ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .resiliencePattern("BULKHEAD")
                .build();
    }

    private java.util.concurrent.CompletableFuture<String> timeoutFallback(String endpoint, long delayMs, Exception ex) {
        log.warn("Timeout fallback triggered for endpoint: {}, delay: {}ms", endpoint, delayMs, ex);
        return java.util.concurrent.CompletableFuture.completedFuture(
                "Timeout fallback response for " + endpoint
        );
    }

    private CircuitBreakerResultDto combinedFallback(ExternalApiRequestDto request, Exception ex) {
        log.warn("Combined resilience patterns fallback triggered", ex);
        return CircuitBreakerResultDto.builder()
                .success(false)
                .message("Combined fallback: " + ex.getMessage())
                .circuitBreakerName("combinedCircuitBreaker")
                .state(getCircuitBreakerState("combinedCircuitBreaker"))
                .error(ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .resiliencePattern("COMBINED")
                .build();
    }

    // ==================== 管理方法 ====================

    /**
     * 获取熔断器状态
     */
    public String getCircuitBreakerState(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        return circuitBreaker.getState().toString();
    }

    /**
     * 获取所有熔断器名称
     */
    public Set<String> getAllCircuitBreakerNames() {
        Set<String> names = new HashSet<>();
        for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
            names.add(cb.getName());
        }
        return names;
    }

    /**
     * 重置熔断器到关闭状态
     */
    public void resetCircuitBreaker(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        circuitBreaker.reset();
        log.info("Reset circuit breaker: {}", name);
    }

    /**
     * 获取默认限流配置
     */
    public CallerRateLimit getDefaultRateLimitConfig() {
        return CallerRateLimit.defaultConfig();
    }

    /**
     * 创建自定义限流配置
     */
    public CallerRateLimit createCustomRateLimit(String caller, int limit, int refreshPeriod, int timeout) {
        return CallerRateLimit.builder()
                .caller(caller)
                .limitForPeriod(limit)
                .limitRefreshPeriodInSeconds(refreshPeriod)
                .timeoutDurationInSeconds(timeout)
                .build();
    }
}
