package com.example.demo.circuitbreaker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 熔断器指标 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDto {

    /**
     * 熔断器名称
     */
    private String circuitBreakerName;

    /**
     * 总调用次数
     */
    private long totalCalls;

    /**
     * 成功调用次数
     */
    private long successfulCalls;

    /**
     * 失败调用次数
     */
    private long failedCalls;

    /**
     * 被拒绝的调用次数（熔断器打开时）
     */
    private long rejectedCalls;

    /**
     * 失败率百分比
     */
    private double failureRate;

    /**
     * 平均调用时长（毫秒）
     */
    private double averageCallDurationMs;

    /**
     * 慢调用次数
     */
    private long slowCalls;

    /**
     * 慢调用率百分比
     */
    private double slowCallRate;

    /**
     * 限流器指标（按名称）
     */
    private Map<String, RateLimiterMetrics> rateLimiterMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimiterMetrics {
        private int limitForPeriod;
        private int availablePermissions;
        private long waitingThreads;
    }
}
