package com.example.demo.virtual.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CompletableFuture 与 StructuredTaskScope 对比结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredTaskScopeCompareResult {

    /**
     * 对比结果 ID
     */
    private String resultId;

    /**
     * 执行时间
     */
    private LocalDateTime executionTime;

    /**
     * CompletableFuture 版本耗时（毫秒）
     */
    private Long completableFutureDurationMillis;

    /**
     * StructuredTaskScope 版本耗时（毫秒）
     */
    private Long structuredTaskScopeDurationMillis;

    /**
     * CompletableFuture 版本结果
     */
    private OrderAggregationResult completableFutureResult;

    /**
     * StructuredTaskScope 版本结果
     */
    private OrderAggregationResult structuredTaskScopeResult;

    /**
     * 行为差异说明
     */
    private String behaviorDifference;
}
