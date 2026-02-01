package com.example.demo.circuitbreaker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 熔断器执行结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerResultDto {

    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 熔断器名称
     */
    private String circuitBreakerName;

    /**
     * 熔断器状态
     */
    private String state;

    /**
     * 执行时间（毫秒）
     */
    private long executionTimeMs;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 异常信息（如果失败）
     */
    private String error;

    /**
     * 使用的容错模式
     */
    private String resiliencePattern;
}
