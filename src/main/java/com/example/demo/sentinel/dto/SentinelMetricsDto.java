package com.example.demo.sentinel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Sentinel 统计指标 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelMetricsDto {

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 通过的 QPS
     */
    private long passQps;

    /**
     * 被拒绝的 QPS
     */
    private long blockQps;

    /**
     * 总请求数
     */
    private long totalRequest;

    /**
     * 异常 QPS
     */
    private long exceptionQps;

    /**
     * 成功率
     */
    private String successRate;

    /**
     * 平均响应时间（毫秒）
     */
    private long averageRt;

    /**
     * 当前并发数
     */
    private long concurrency;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
