package com.example.demo.virtual.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付状态竞速结果
 * 多个支付网关并行查询，第一个成功即返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRaceResult {

    /**
     * 结果 ID
     */
    private String resultId;

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 执行时间
     */
    private LocalDateTime executionTime;

    /**
     * 总执行时长（毫秒）
     */
    private Long totalDurationMillis;

    /**
     * 胜出的支付信息
     */
    private OrderAggregationResult.PaymentInfo winner;

    /**
     * 胜出的网关名称
     */
    private String winnerGateway;

    /**
     * 各网关查询结果
     */
    @Builder.Default
    private List<GatewayResult> gatewayResults = new ArrayList<>();

    /**
     * 整体是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 网关查询结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayResult {
        /**
         * 网关名称
         */
        private String gatewayName;

        /**
         * 状态
         */
        private OrderAggregationResult.SubTaskStatus status;

        /**
         * 执行时长（毫秒）
         */
        private Long durationMillis;

        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
