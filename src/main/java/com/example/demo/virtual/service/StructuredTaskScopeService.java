package com.example.demo.virtual.service;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqUserRepository;
import com.example.demo.virtual.dto.OrderAggregationResult;
import com.example.demo.virtual.dto.OrderAggregationResult.*;
import com.example.demo.virtual.dto.PaymentRaceResult;
import com.example.demo.virtual.dto.StructuredTaskScopeCompareResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;

/**
 * 基于 JDK StructuredTaskScope API 的结构化并发服务
 * 使用真实的 StructuredTaskScope 替代 CompletableFuture 模拟
 */
@Service
@Slf4j
public class StructuredTaskScopeService {

    @Resource
    private JooqUserRepository userRepository;

    @Resource
    private JooqOrderRepository orderRepository;

    /**
     * ShutdownOnFailure — 订单详情聚合
     * 并行获取用户信息、订单项、支付状态，任一失败即取消全部
     */
    public OrderAggregationResult aggregateOrderDetails(Long orderId, Long userId) {
        String resultId = UUID.randomUUID().toString();
        LocalDateTime executionTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        log.info("Starting ShutdownOnFailure aggregation for order: {}, user: {}", orderId, userId);

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // 三个并行子任务
            StructuredTaskScope.Subtask<UserInfo> userTask = scope.fork(() -> fetchUserInfo(userId));
            StructuredTaskScope.Subtask<OrderItemsInfo> orderTask = scope.fork(() -> fetchOrderItems(orderId));
            StructuredTaskScope.Subtask<PaymentInfo> paymentTask = scope.fork(() -> fetchPaymentStatus(orderId));

            // 等待所有任务完成或任一失败
            scope.join();
            scope.throwIfFailed(e -> new RuntimeException("Subtask failed", e));

            // 全部成功，收集结果
            UserInfo userInfo = userTask.get();
            OrderItemsInfo orderItemsInfo = orderTask.get();
            PaymentInfo paymentInfo = paymentTask.get();

            long totalDuration = System.currentTimeMillis() - startTime;

            List<SubTaskResult> subTaskResults = List.of(
                    buildSubTaskResult("FetchUserInfo", SubTaskStatus.SUCCESS,
                            totalDuration, null),
                    buildSubTaskResult("FetchOrderItems", SubTaskStatus.SUCCESS,
                            totalDuration, null),
                    buildSubTaskResult("FetchPaymentStatus", SubTaskStatus.SUCCESS,
                            totalDuration, null)
            );

            log.info("ShutdownOnFailure aggregation completed in {} ms", totalDuration);

            return OrderAggregationResult.builder()
                    .resultId(resultId)
                    .orderId(orderId)
                    .executionTime(executionTime)
                    .totalDurationMillis(totalDuration)
                    .strategy(AggregationStrategy.SHUTDOWN_ON_FAILURE)
                    .subTaskResults(subTaskResults)
                    .userInfo(userInfo)
                    .orderItemsInfo(orderItemsInfo)
                    .paymentInfo(paymentInfo)
                    .success(true)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Aggregation interrupted", e);
            return buildErrorResult(resultId, orderId, executionTime, startTime,
                    "Aggregation interrupted: " + e.getMessage());
        } catch (Exception e) {
            log.error("Aggregation failed", e);
            return buildErrorResult(resultId, orderId, executionTime, startTime,
                    "Aggregation failed: " + e.getMessage());
        }
    }

    /**
     * ShutdownOnSuccess — 竞速获取支付状态
     * 多个支付网关并行查询，第一个成功即返回
     */
    public PaymentRaceResult racePaymentStatus(Long orderId) {
        String resultId = UUID.randomUUID().toString();
        LocalDateTime executionTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        log.info("Starting ShutdownOnSuccess race for order: {}", orderId);

        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<PaymentInfo>()) {

            // 三个模拟支付网关，不同延迟
            scope.fork(() -> queryPaymentGateway(orderId, "FastGateway", 50, false));
            scope.fork(() -> queryPaymentGateway(orderId, "MediumGateway", 150, false));
            scope.fork(() -> queryPaymentGateway(orderId, "SlowGateway", 500, false));

            // 等待第一个成功
            scope.join();

            PaymentInfo winner = scope.result();

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("Race completed in {} ms, winner: {}", totalDuration,
                    winner != null ? winner.getGatewayName() : "none");

            List<PaymentRaceResult.GatewayResult> gatewayResults = buildGatewayResults(
                    scope, List.of("FastGateway", "MediumGateway", "SlowGateway"), totalDuration);

            return PaymentRaceResult.builder()
                    .resultId(resultId)
                    .orderId(orderId)
                    .executionTime(executionTime)
                    .totalDurationMillis(totalDuration)
                    .winner(winner)
                    .winnerGateway(winner != null ? winner.getGatewayName() : null)
                    .gatewayResults(gatewayResults)
                    .success(true)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return buildRaceErrorResult(resultId, orderId, executionTime, startTime,
                    "Race interrupted");
        } catch (Exception e) {
            return buildRaceErrorResult(resultId, orderId, executionTime, startTime,
                    "Race failed: " + e.getMessage());
        }
    }

    /**
     * 自定义 Scope — 降级聚合
     * 核心数据源（用户信息、订单项）必须成功，非核心数据源（支付状态）失败不影响整体
     */
    public OrderAggregationResult aggregateWithDegradation(Long orderId, Long userId) {
        String resultId = UUID.randomUUID().toString();
        LocalDateTime executionTime = LocalDateTime.now();
        long startTime = System.currentTimeMillis();

        log.info("Starting degraded aggregation for order: {}, user: {}", orderId, userId);

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // 核心子任务
            StructuredTaskScope.Subtask<UserInfo> userTask = scope.fork(() -> fetchUserInfo(userId));
            StructuredTaskScope.Subtask<OrderItemsInfo> orderTask = scope.fork(() -> fetchOrderItems(orderId));

            // 非核心子任务：支付状态（可能失败，不影响整体）
            StructuredTaskScope.Subtask<PaymentInfo> paymentTask = scope.fork(() -> fetchPaymentStatus(orderId));

            scope.join();

            // 检查核心任务是否失败
            if (userTask.state() == StructuredTaskScope.Subtask.State.FAILED
                    || orderTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                scope.throwIfFailed(e -> new RuntimeException("Critical subtask failed", e));
            }

            UserInfo userInfo = userTask.get();
            OrderItemsInfo orderItemsInfo = orderTask.get();

            // 支付状态：失败时降级，不影响整体
            boolean degraded = false;
            PaymentInfo paymentInfo = null;
            if (paymentTask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                paymentInfo = paymentTask.get();
            } else {
                degraded = true;
                log.warn("Payment status fetch failed, degrading result");
            }

            long totalDuration = System.currentTimeMillis() - startTime;

            List<SubTaskResult> subTaskResults = new ArrayList<>();
            subTaskResults.add(buildSubTaskResult("FetchUserInfo", SubTaskStatus.SUCCESS, totalDuration, null));
            subTaskResults.add(buildSubTaskResult("FetchOrderItems", SubTaskStatus.SUCCESS, totalDuration, null));
            subTaskResults.add(buildSubTaskResult("FetchPaymentStatus",
                    degraded ? SubTaskStatus.FAILED : SubTaskStatus.SUCCESS,
                    totalDuration, degraded ? "Payment API unavailable" : null));

            log.info("Degraded aggregation completed in {} ms, degraded: {}", totalDuration, degraded);

            return OrderAggregationResult.builder()
                    .resultId(resultId)
                    .orderId(orderId)
                    .executionTime(executionTime)
                    .totalDurationMillis(totalDuration)
                    .strategy(AggregationStrategy.DEGRADED_AGGREGATION)
                    .subTaskResults(subTaskResults)
                    .userInfo(userInfo)
                    .orderItemsInfo(orderItemsInfo)
                    .paymentInfo(paymentInfo)
                    .success(true)
                    .degraded(degraded)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return buildErrorResult(resultId, orderId, executionTime, startTime,
                    "Degraded aggregation interrupted");
        } catch (Exception e) {
            return buildErrorResult(resultId, orderId, executionTime, startTime,
                    "Degraded aggregation failed: " + e.getMessage());
        }
    }

    /**
     * 对比 CompletableFuture 和 StructuredTaskScope
     */
    public StructuredTaskScopeCompareResult compareApproaches(Long orderId, Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ========== 内部方法 ==========

    /**
     * 获取用户信息
     */
    private UserInfo fetchUserInfo(Long userId) {
        log.info("Fetching user info for userId: {}", userId);
        long start = System.currentTimeMillis();

        JooqUserDto user = userRepository.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        long duration = System.currentTimeMillis() - start;
        log.info("User info fetched in {} ms", duration);

        return UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    /**
     * 获取订单项信息
     */
    private OrderItemsInfo fetchOrderItems(Long orderId) {
        log.info("Fetching order items for orderId: {}", orderId);
        long start = System.currentTimeMillis();

        JooqOrderDto order = orderRepository.findOrderWithItemsById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        long duration = System.currentTimeMillis() - start;
        log.info("Order items fetched in {} ms", duration);

        List<OrderItemDetail> items = order.getItems() != null
                ? order.getItems().stream()
                .map(this::toOrderItemDetail)
                .toList()
                : List.of();

        return OrderItemsInfo.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus())
                .items(items)
                .build();
    }

    /**
     * 获取支付状态（模拟外部 API）
     */
    private PaymentInfo fetchPaymentStatus(Long orderId) {
        log.info("Fetching payment status for orderId: {}", orderId);
        long start = System.currentTimeMillis();

        // 模拟外部 API 延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment API interrupted", e);
        }

        long duration = System.currentTimeMillis() - start;
        log.info("Payment status fetched in {} ms", duration);

        return PaymentInfo.builder()
                .paymentStatus("PAID")
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8))
                .paymentTime(LocalDateTime.now())
                .gatewayName("SimulatedGateway")
                .build();
    }

    private OrderItemDetail toOrderItemDetail(JooqOrderItemDto dto) {
        return OrderItemDetail.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .subtotal(dto.getSubtotal())
                .build();
    }

    private SubTaskResult buildSubTaskResult(String taskName, SubTaskStatus status,
                                              Long durationMillis, String errorMessage) {
        return SubTaskResult.builder()
                .taskName(taskName)
                .status(status)
                .durationMillis(durationMillis)
                .errorMessage(errorMessage)
                .build();
    }

    private OrderAggregationResult buildErrorResult(String resultId, Long orderId,
                                                     LocalDateTime executionTime, long startTime,
                                                     String errorMessage) {
        return OrderAggregationResult.builder()
                .resultId(resultId)
                .orderId(orderId)
                .executionTime(executionTime)
                .totalDurationMillis(System.currentTimeMillis() - startTime)
                .strategy(AggregationStrategy.SHUTDOWN_ON_FAILURE)
                .subTaskResults(new ArrayList<>())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 查询支付网关（模拟）
     */
    private PaymentInfo queryPaymentGateway(Long orderId, String gatewayName,
                                             int delayMillis, boolean shouldFail) {
        log.info("Querying gateway: {} for order: {}", gatewayName, orderId);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(gatewayName + " interrupted", e);
        }

        if (shouldFail) {
            throw new RuntimeException(gatewayName + " unavailable");
        }

        log.info("Gateway {} responded after {} ms", gatewayName, delayMillis);
        return PaymentInfo.builder()
                .paymentStatus("PAID")
                .transactionId("TXN-" + gatewayName + "-" + orderId)
                .paymentTime(LocalDateTime.now())
                .gatewayName(gatewayName)
                .build();
    }

    /**
     * 构建网关结果列表
     */
    private List<PaymentRaceResult.GatewayResult> buildGatewayResults(
            StructuredTaskScope.ShutdownOnSuccess<PaymentInfo> scope,
            List<String> gatewayNames, long totalDuration) {
        return gatewayNames.stream()
                .map(name -> PaymentRaceResult.GatewayResult.builder()
                        .gatewayName(name)
                        .status(SubTaskStatus.SUCCESS)
                        .durationMillis(totalDuration)
                        .build())
                .toList();
    }

    private PaymentRaceResult buildRaceErrorResult(String resultId, Long orderId,
                                                    LocalDateTime executionTime, long startTime,
                                                    String errorMessage) {
        return PaymentRaceResult.builder()
                .resultId(resultId)
                .orderId(orderId)
                .executionTime(executionTime)
                .totalDurationMillis(System.currentTimeMillis() - startTime)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
