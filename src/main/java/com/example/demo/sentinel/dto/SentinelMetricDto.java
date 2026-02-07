package com.example.demo.sentinel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Sentinel 指标信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelMetricDto {

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 总请求数
     */
    private long totalRequests;

    /**
     * 成功请求数
     */
    private long successRequests;

    /**
     * 被拒绝的请求数（流控拦截）
     */
    private long blockedRequests;

    /**
     * 当前 QPS
     */
    private double currentQps;

    /**
     * 平均响应时间（毫秒）
     */
    private double averageResponseTimeMs;

    /**
     * 最小响应时间（毫秒）
     */
    private double minResponseTimeMs;

    /**
     * 最大响应时间（毫秒）
     */
    private double maxResponseTimeMs;

    /**
     * 异常总数
     */
    private long exceptionCount;

    /**
     * 异常比例（百分比）
     */
    private double exceptionRate;

    /**
     * 线程池使用情况
     */
    private ThreadPoolMetrics threadPoolMetrics;

    /**
     * 各资源指标统计
     */
    private Map<String, ResourceMetrics> resourceMetrics;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 线程池指标内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadPoolMetrics {
        /**
         * 活跃线程数
         */
        private int activeThreads;

        /**
         * 池大小
         */
        private int poolSize;

        /**
         * 核心线程数
         */
        private int corePoolSize;

        /**
         * 最大线程数
         */
        private int maximumPoolSize;
    }

    /**
     * 资源指标内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceMetrics {
        /**
         * 资源名称
         */
        private String resource;

        /**
         * 通过的请求数
         */
        private long passCount;

        /**
         * 被阻塞的请求数
         */
        private long blockCount;

        /**
         * 完成的请求数
         */
        private long completeCount;

        /**
         * 异常数
         */
        private long exceptionCount;

        /**
         * 平均响应时间
         */
        private double avgResponseTime;
    }
}
