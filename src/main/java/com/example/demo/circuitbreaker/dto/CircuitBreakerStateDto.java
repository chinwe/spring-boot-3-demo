package com.example.demo.circuitbreaker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 熔断器状态 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerStateDto {

    /**
     * 熔断器名称
     */
    private String name;

    /**
     * 当前状态：CLOSED, OPEN, HALF_OPEN
     */
    private String state;

    /**
     * 失败率百分比
     */
    private double failureRate;

    /**
     * 滑动窗口大小
     */
    private int slidingWindowSize;

    /**
     * 当前调用数
     */
    private int numberOfCalls;

    /**
     * 失败调用数
     */
    private int numberOfFailedCalls;

    /**
     * 成功调用数
     */
    private int numberOfSuccessfulCalls;

    /**
     * 是否允许调用
     */
    private boolean callPermitted;

    /**
     * 剩余等待时间（毫秒），用于OPEN状态
     */
    private Long remainingWaitTimeMs;
}
