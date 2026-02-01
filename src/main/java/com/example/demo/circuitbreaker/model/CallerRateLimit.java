package com.example.demo.circuitbreaker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 调用方限流配置模型
 * 用于存储特定调用方的限流配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallerRateLimit {

    /**
     * 调用方标识（如：mobile, web, admin）
     */
    private String caller;

    /**
     * 限流周期内请求数
     */
    private int limitForPeriod;

    /**
     * 限流刷新周期（秒）
     */
    private int limitRefreshPeriodInSeconds;

    /**
     * 超时等待时间（秒）
     */
    private int timeoutDurationInSeconds;

    /**
     * 获取限流刷新周期（Duration）
     */
    public Duration getLimitRefreshPeriod() {
        return Duration.ofSeconds(limitRefreshPeriodInSeconds);
    }

    /**
     * 获取超时等待时间（Duration）
     */
    public Duration getTimeoutDuration() {
        return Duration.ofSeconds(timeoutDurationInSeconds);
    }

    /**
     * 创建默认配置
     */
    public static CallerRateLimit defaultConfig() {
        return new CallerRateLimit("default", 10, 1, 5);
    }

    /**
     * 创建宽松配置（用于高优先级调用方）
     */
    public static CallerRateLimit relaxedConfig() {
        return new CallerRateLimit("relaxed", 100, 1, 10);
    }

    /**
     * 创建严格配置（用于低优先级调用方）
     */
    public static CallerRateLimit strictConfig() {
        return new CallerRateLimit("strict", 5, 1, 3);
    }
}
