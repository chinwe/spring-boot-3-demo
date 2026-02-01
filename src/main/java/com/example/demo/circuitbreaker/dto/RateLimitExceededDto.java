package com.example.demo.circuitbreaker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 限流超出响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitExceededDto {

    /**
     * 调用方标识
     */
    private String caller;

    /**
     * 限流器名称
     */
    private String rateLimiterName;

    /**
     * 限流周期内请求数
     */
    private int limitForPeriod;

    /**
     * 周期内已用请求数
     */
    private int availablePermissions;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 重试建议时间（秒）
     */
    private Integer retryAfterSeconds;
}
