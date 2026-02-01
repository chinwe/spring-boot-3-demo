package com.example.demo.circuitbreaker.service;

import com.example.demo.circuitbreaker.annotation.CallerRateLimiter;
import com.example.demo.circuitbreaker.dto.CircuitBreakerResultDto;
import com.example.demo.circuitbreaker.model.CallerRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 基于 X-Caller 的限流服务
 * 演示差异化限流功能
 */
@Service
@Slf4j
public class CallerRateLimiterService {

    /**
     * 基础限流示例（使用默认配置）
     */
    @CallerRateLimiter(
            prefix = "basicLimiter",
            defaultLimitForPeriod = 10
    )
    public String basicRateLimitedCall(String data) {
        log.info("Processing basic rate limited call for data: {}", data);
        return String.format("Processed: %s at %s", data, LocalDateTime.now());
    }

    /**
     * 差异化限流示例
     * mobile: 100 请求/秒
     * web: 50 请求/秒
     * admin: 1000 请求/秒
     * 其他: 10 请求/秒（默认）
     */
    @CallerRateLimiter(
            prefix = "callerLimiter",
            defaultLimitForPeriod = 10,
            callerConfigs = "mobile=100,1,5;web=50,1,5;admin=1000,1,10"
    )
    public CircuitBreakerResultDto callerSpecificRateLimitedCall(String operation) {
        log.info("Processing caller-specific rate limited operation: {}", operation);

        return CircuitBreakerResultDto.builder()
                .success(true)
                .message(String.format("Operation '%s' completed successfully", operation))
                .timestamp(LocalDateTime.now())
                .resiliencePattern("CALLER_RATE_LIMITER")
                .build();
    }

    /**
     * 从参数获取调用方标识的限流示例
     */
    @CallerRateLimiter(
            prefix = "paramLimiter",
            defaultLimitForPeriod = 20,
            callerParamName = "callerId",
            callerConfigs = "premium=100,1,5;free=5,1,3"
    )
    public CircuitBreakerResultDto rateLimitedCallWithCallerParam(String callerId, String operation) {
        log.info("Processing operation '{}' for caller: {}", operation, callerId);

        return CircuitBreakerResultDto.builder()
                .success(true)
                .message(String.format("Operation '%s' completed for caller '%s'", operation, callerId))
                .timestamp(LocalDateTime.now())
                .resiliencePattern("PARAM_RATE_LIMITER")
                .build();
    }

    /**
     * 严格限流示例
     */
    @CallerRateLimiter(
            prefix = "strictLimiter",
            defaultLimitForPeriod = 3,
            defaultLimitRefreshPeriodInSeconds = 1,
            defaultTimeoutDurationInSeconds = 2
    )
    public String strictRateLimitedCall(String data) {
        log.info("Processing strict rate limited call for data: {}", data);
        return String.format("Strict limited: %s at %s", data, LocalDateTime.now());
    }

    /**
     * 宽松限流示例
     */
    @CallerRateLimiter(
            prefix = "relaxedLimiter",
            defaultLimitForPeriod = 1000,
            defaultLimitRefreshPeriodInSeconds = 1,
            defaultTimeoutDurationInSeconds = 10
    )
    public String relaxedRateLimitedCall(String data) {
        log.info("Processing relaxed rate limited call for data: {}", data);
        return String.format("Relaxed limited: %s at %s", data, LocalDateTime.now());
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

    /**
     * 获取推荐配置
     */
    public CallerRateLimit getRecommendedConfigForCaller(String caller) {
        return switch (caller.toLowerCase()) {
            case "mobile" -> CallerRateLimit.builder()
                    .caller("mobile")
                    .limitForPeriod(100)
                    .limitRefreshPeriodInSeconds(1)
                    .timeoutDurationInSeconds(5)
                    .build();
            case "web" -> CallerRateLimit.builder()
                    .caller("web")
                    .limitForPeriod(50)
                    .limitRefreshPeriodInSeconds(1)
                    .timeoutDurationInSeconds(5)
                    .build();
            case "admin" -> CallerRateLimit.builder()
                    .caller("admin")
                    .limitForPeriod(1000)
                    .limitRefreshPeriodInSeconds(1)
                    .timeoutDurationInSeconds(10)
                    .build();
            case "api" -> CallerRateLimit.builder()
                    .caller("api")
                    .limitForPeriod(500)
                    .limitRefreshPeriodInSeconds(1)
                    .timeoutDurationInSeconds(5)
                    .build();
            default -> CallerRateLimit.defaultConfig();
        };
    }
}
