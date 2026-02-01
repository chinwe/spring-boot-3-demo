package com.example.demo.circuitbreaker.service;

import com.example.demo.circuitbreaker.dto.CircuitBreakerStateDto;
import com.example.demo.circuitbreaker.dto.MetricsDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 熔断器指标服务
 * 收集和查询熔断器、限流器、舱壁隔离的指标
 */
@Service
@Slf4j
public class CircuitBreakerMetricsService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    // 自定义指标计数器
    private long totalSuccessfulCalls = 0;
    private long totalFailedCalls = 0;
    private long totalRejectedCalls = 0;

    public CircuitBreakerMetricsService(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RateLimiterRegistry rateLimiterRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    /**
     * 获取熔断器状态
     */
    public CircuitBreakerStateDto getCircuitBreakerState(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);

        io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        return CircuitBreakerStateDto.builder()
                .name(name)
                .state(circuitBreaker.getState().toString())
                .failureRate(metrics.getFailureRate())
                .slidingWindowSize(metrics.getNumberOfBufferedCalls())
                .numberOfCalls(metrics.getNumberOfBufferedCalls())
                .numberOfFailedCalls(metrics.getNumberOfFailedCalls())
                .numberOfSuccessfulCalls(metrics.getNumberOfSuccessfulCalls())
                .callPermitted(circuitBreaker.tryAcquirePermission())
                .remainingWaitTimeMs(circuitBreaker.getState() == CircuitBreaker.State.OPEN ?
                        getRemainingWaitTime(circuitBreaker) : null)
                .build();
    }

    /**
     * 获取所有熔断器状态
     */
    public Map<String, CircuitBreakerStateDto> getAllCircuitBreakerStates() {
        return circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .collect(Collectors.toMap(
                        CircuitBreaker::getName,
                        cb -> getCircuitBreakerState(cb.getName())
                ));
    }

    /**
     * 获取熔断器指标
     */
    public MetricsDto getCircuitBreakerMetrics(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        return MetricsDto.builder()
                .circuitBreakerName(name)
                .totalCalls(metrics.getNumberOfBufferedCalls())
                .successfulCalls(metrics.getNumberOfSuccessfulCalls())
                .failedCalls(metrics.getNumberOfFailedCalls())
                .rejectedCalls(metrics.getNumberOfNotPermittedCalls())
                .failureRate(metrics.getFailureRate())
                .averageCallDurationMs(metrics.getSlowCallRate())
                .slowCalls((long) metrics.getSlowCallRate())
                .slowCallRate(metrics.getSlowCallRate())
                .build();
    }

    /**
     * 获取所有指标（包括限流器）
     */
    public MetricsDto getAllMetrics(String circuitBreakerName) {
        MetricsDto metrics = getCircuitBreakerMetrics(circuitBreakerName);

        // 添加限流器指标
        Map<String, MetricsDto.RateLimiterMetrics> rateLimiterMetrics = new HashMap<>();
        for (RateLimiter rateLimiter : rateLimiterRegistry.getAllRateLimiters()) {
            io.github.resilience4j.ratelimiter.RateLimiter.Metrics rlMetrics = rateLimiter.getMetrics();
            rateLimiterMetrics.put(rateLimiter.getName(), new MetricsDto.RateLimiterMetrics(
                    rateLimiter.getRateLimiterConfig().getLimitForPeriod(),
                    rlMetrics.getAvailablePermissions(),
                    rlMetrics.getNumberOfWaitingThreads()
            ));
        }

        metrics.setRateLimiterMetrics(rateLimiterMetrics);
        return metrics;
    }

    /**
     * 获取舱壁指标
     */
    public Map<String, Object> getBulkheadMetrics() {
        Map<String, Object> bulkheadMetrics = new HashMap<>();

        for (Bulkhead bulkhead : bulkheadRegistry.getAllBulkheads()) {
            Map<String, Object> metrics = new HashMap<>();
            io.github.resilience4j.bulkhead.Bulkhead.Metrics bMetrics = bulkhead.getMetrics();

            metrics.put("maxConcurrentCalls", bulkhead.getBulkheadConfig().getMaxConcurrentCalls());
            metrics.put("maxWaitDuration", bulkhead.getBulkheadConfig().getMaxWaitDuration().toMillis());
            metrics.put("availableConcurrentCalls", bMetrics.getAvailableConcurrentCalls());
            metrics.put("maxAllowedConcurrentCalls", bMetrics.getMaxAllowedConcurrentCalls());

            bulkheadMetrics.put(bulkhead.getName(), metrics);
        }

        return bulkheadMetrics;
    }

    /**
     * 记录成功调用
     */
    public void recordSuccess() {
        totalSuccessfulCalls++;
    }

    /**
     * 记录失败调用
     */
    public void recordFailure() {
        totalFailedCalls++;
    }

    /**
     * 记录拒绝调用
     */
    public void recordRejection() {
        totalRejectedCalls++;
    }

    /**
     * 获取总体统计
     */
    public Map<String, Long> getOverallStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalSuccessfulCalls", totalSuccessfulCalls);
        stats.put("totalFailedCalls", totalFailedCalls);
        stats.put("totalRejectedCalls", totalRejectedCalls);
        stats.put("totalCalls", totalSuccessfulCalls + totalFailedCalls + totalRejectedCalls);
        return stats;
    }

    /**
     * 重置指标
     */
    public void resetMetrics() {
        totalSuccessfulCalls = 0;
        totalFailedCalls = 0;
        totalRejectedCalls = 0;
        log.info("Metrics reset successfully");
    }

    /**
     * 重置特定熔断器
     */
    public void resetCircuitBreaker(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        circuitBreaker.reset();
        log.info("Circuit breaker '{}' reset successfully", name);
    }

    /**
     * 获取健康状态摘要
     */
    public Map<String, String> getHealthSummary() {
        Map<String, String> summary = new HashMap<>();

        // 熔断器状态
        long openCount = circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .filter(cb -> cb.getState() == CircuitBreaker.State.OPEN)
                .count();
        long halfOpenCount = circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .filter(cb -> cb.getState() == CircuitBreaker.State.HALF_OPEN)
                .count();
        long closedCount = circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .filter(cb -> cb.getState() == CircuitBreaker.State.CLOSED)
                .count();

        summary.put("circuitBreakers", String.format("Open: %d, Half-Open: %d, Closed: %d",
                openCount, halfOpenCount, closedCount));

        // 限流器状态
        summary.put("rateLimiters", String.format("Total: %d", rateLimiterRegistry.getAllRateLimiters().size()));

        // 舱壁状态
        summary.put("bulkheads", String.format("Total: %d", bulkheadRegistry.getAllBulkheads().size()));

        return summary;
    }

    /**
     * 获取熔断器剩余等待时间
     */
    private Long getRemainingWaitTime(CircuitBreaker circuitBreaker) {
        try {
            return 30000L; // 默认30秒
        } catch (Exception e) {
            return null;
        }
    }
}
