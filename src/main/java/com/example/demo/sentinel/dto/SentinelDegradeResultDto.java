package com.example.demo.sentinel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Sentinel 降级结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelDegradeResultDto {

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 是否被降级
     */
    private boolean degraded;

    /**
     * 原始方法执行结果
     */
    private String originalResult;

    /**
     * 降级后的返回结果
     */
    private String fallbackResult;

    /**
     * 降级原因
     */
    private String degradeReason;

    /**
     * 熔断器状态：CLOSED（关闭）、OPEN（打开）、HALF_OPEN（半开）
     */
    private String circuitState;

    /**
     * 熔断器状态描述
     */
    private String circuitStateDescription;

    /**
     * 预计恢复时间（毫秒）
     */
    private long estimatedRecoveryTimeMs;

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
     * 异常信息（如果执行失败）
     */
    private String exceptionMessage;
}
