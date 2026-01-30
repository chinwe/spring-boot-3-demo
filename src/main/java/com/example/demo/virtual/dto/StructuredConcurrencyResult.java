package com.example.demo.virtual.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 结构化并发结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredConcurrencyResult {

    /**
     * 结果 ID
     */
    private String resultId;

    /**
     * 执行时间
     */
    private LocalDateTime executionTime;

    /**
     * 总执行时长（毫秒）
     */
    private Long totalDurationMillis;

    /**
     * 并发策略
     */
    private ConcurrencyStrategy strategy;

    /**
     * 任务结果列表
     */
    @Builder.Default
    private List<TaskResult> taskResults = new ArrayList<>();

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResult {
        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 任务状态
         */
        private TaskStatus status;

        /**
         * 执行时长（毫秒）
         */
        private Long durationMillis;

        /**
         * 结果
         */
        private String result;

        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 任务状态
     */
    public enum TaskStatus {
        SUCCESS,
        FAILED,
        CANCELLED
    }

    /**
     * 并发策略
     */
    public enum ConcurrencyStrategy {
        /**
         * 等待所有任务完成
         */
        JOIN_ALL,

        /**
         * 任一成功即关闭
         */
        SHUTDOWN_ON_SUCCESS,

        /**
         * 任一失败即关闭
         */
        SHUTDOWN_ON_FAILURE
    }
}
