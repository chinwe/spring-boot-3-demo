package com.example.demo.virtual.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合结果
 * 由 StructuredTaskScope 并行获取用户信息、订单项和支付状态后组装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAggregationResult {

    /**
     * 聚合结果 ID
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
     * 使用的并发策略
     */
    private AggregationStrategy strategy;

    /**
     * 各子任务结果
     */
    @Builder.Default
    private List<SubTaskResult> subTaskResults = new ArrayList<>();

    /**
     * 用户信息（来自 j_users）
     */
    private UserInfo userInfo;

    /**
     * 订单项列表（来自 j_order_items + j_products）
     */
    private OrderItemsInfo orderItemsInfo;

    /**
     * 支付状态（来自模拟外部 API）
     */
    private PaymentInfo paymentInfo;

    /**
     * 整体是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否为降级结果（非关键数据源失败但仍有基本数据）
     */
    @Builder.Default
    private Boolean degraded = false;

    /**
     * 子任务结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTaskResult {
        /**
         * 子任务名称
         */
        private String taskName;

        /**
         * 子任务状态
         */
        private SubTaskStatus status;

        /**
         * 执行时长（毫秒）
         */
        private Long durationMillis;

        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 子任务状态
     */
    public enum SubTaskStatus {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAILED,
        /**
         * 被取消（因其他子任务触发关闭策略）
         */
        CANCELLED
    }

    /**
     * 聚合策略
     */
    public enum AggregationStrategy {
        /**
         * 任一失败即取消全部（ShutdownOnFailure）
         */
        SHUTDOWN_ON_FAILURE,

        /**
         * 任一成功即取消其他（ShutdownOnSuccess）
         */
        SHUTDOWN_ON_SUCCESS,

        /**
         * 降级聚合（自定义 Scope）
         */
        DEGRADED_AGGREGATION
    }

    /**
     * 用户信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String email;
        private String phone;
    }

    /**
     * 订单项信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemsInfo {
        private Long orderId;
        private String orderNumber;
        private String orderStatus;
        private List<OrderItemDetail> items;
    }

    /**
     * 订单项详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Long productId;
        private String productName;
        private Integer quantity;
        private java.math.BigDecimal price;
        private java.math.BigDecimal subtotal;
    }

    /**
     * 支付信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        /**
         * 支付状态
         */
        private String paymentStatus;

        /**
         * 交易号
         */
        private String transactionId;

        /**
         * 支付时间
         */
        private LocalDateTime paymentTime;

        /**
         * 支付网关名称（竞速模式下标识哪个网关先返回）
         */
        private String gatewayName;
    }
}
