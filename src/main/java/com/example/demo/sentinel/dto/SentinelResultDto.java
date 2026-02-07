package com.example.demo.sentinel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Sentinel 执行结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelResultDto {

    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 调用次数
     */
    private long callCount;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 执行时间（毫秒）
     */
    private long executionTimeMs;

    /**
     * 参数信息
     */
    private Map<String, Object> parameters;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 熔断器状态（用于熔断降级）
     */
    private String degradeStatus;

    /**
     * 系统负载类型（用于系统规则）
     */
    private String systemLoadType;

    /**
     * 阻塞异常类型（当被限流时）
     */
    private String blockException;

    /**
     * 异常类型（当业务失败时）
     */
    private String error;

    /**
     * 异常消息
     */
    private String errorMessage;
}
