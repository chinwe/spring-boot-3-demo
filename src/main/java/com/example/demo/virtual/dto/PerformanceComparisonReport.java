package com.example.demo.virtual.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 性能对比报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceComparisonReport {

    /**
     * 报告 ID
     */
    private String reportId;

    /**
     * 测试时间
     */
    private LocalDateTime testTime;

    /**
     * 任务数量
     */
    private Integer taskCount;

    /**
     * 传统线程池结果
     */
    private PerformanceResult traditionalThreadPool;

    /**
     * 虚拟线程结果
     */
    private PerformanceResult virtualThreads;

    /**
     * 性能提升百分比
     */
    private Double improvementPercentage;

    /**
     * 性能指标结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceResult {
        /**
         * 总执行时长（毫秒）
         */
        private Long totalDurationMillis;

        /**
         * 平均任务执行时长（毫秒）
         */
        private Double averageTaskDurationMillis;

        /**
         * 吞吐量（任务/秒）
         */
        private Double throughput;

        /**
         * 峰值线程数
         */
        private Integer peakThreads;

        /**
         * 内存使用（字节）
         */
        private Long memoryUsageBytes;

        /**
         * CPU 时间（毫秒）
         */
        private Long cpuTimeMillis;
    }
}
