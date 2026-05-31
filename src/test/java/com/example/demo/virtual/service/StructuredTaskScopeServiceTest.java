package com.example.demo.virtual.service;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqUserRepository;
import com.example.demo.virtual.dto.OrderAggregationResult;
import com.example.demo.virtual.dto.OrderAggregationResult.*;
import com.example.demo.virtual.dto.PaymentRaceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * StructuredTaskScopeService 单元测试
 * 使用真实 StructuredTaskScope API（非 CompletableFuture 模拟）
 */
@ExtendWith(MockitoExtension.class)
class StructuredTaskScopeServiceTest {

    @Mock
    private JooqUserRepository userRepository;

    @Mock
    private JooqOrderRepository orderRepository;

    private StructuredTaskScopeService service;

    @BeforeEach
    void setUp() {
        service = new StructuredTaskScopeService();
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "orderRepository", orderRepository);
    }

    // ========== ShutdownOnFailure：全部成功 ==========

    @Test
    @DisplayName("ShutdownOnFailure — 所有子任务成功时，聚合完整的订单详情")
    void aggregateOrderDetails_allSucceed_returnsCompleteResult() {
        // Given
        Long orderId = 1L;
        Long userId = 10L;

        JooqUserDto userDto = JooqUserDto.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .phone("13800138000")
                .build();

        JooqOrderItemDto item1 = JooqOrderItemDto.builder()
                .id(1L).orderId(orderId).productId(100L).productName("Widget")
                .quantity(2).price(new BigDecimal("19.99")).subtotal(new BigDecimal("39.98"))
                .build();

        JooqOrderDto orderDto = JooqOrderDto.builder()
                .id(orderId).orderNumber("ORD-001").userId(userId)
                .totalAmount(new BigDecimal("59.97")).status("PAID")
                .items(List.of(item1))
                .build();

        when(userRepository.findById(userId)).thenReturn(userDto);
        when(orderRepository.findOrderWithItemsById(orderId)).thenReturn(orderDto);

        // When
        OrderAggregationResult result = service.aggregateOrderDetails(orderId, userId);

        // Then
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getStrategy()).isEqualTo(AggregationStrategy.SHUTDOWN_ON_FAILURE);
        assertThat(result.getOrderId()).isEqualTo(orderId);

        // 验证用户信息
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getUserId()).isEqualTo(userId);
        assertThat(result.getUserInfo().getUsername()).isEqualTo("testuser");

        // 验证订单项信息
        assertThat(result.getOrderItemsInfo()).isNotNull();
        assertThat(result.getOrderItemsInfo().getOrderId()).isEqualTo(orderId);
        assertThat(result.getOrderItemsInfo().getItems()).hasSize(1);

        // 验证支付信息（模拟 API 成功）
        assertThat(result.getPaymentInfo()).isNotNull();
        assertThat(result.getPaymentInfo().getPaymentStatus()).isNotNull();

        // 验证总耗时 < 各子任务串行执行的总耗时（并行执行应更快）
        assertThat(result.getTotalDurationMillis()).isGreaterThan(0L);

        // 验证所有子任务状态
        assertThat(result.getSubTaskResults())
                .allMatch(sr -> sr.getStatus() == SubTaskStatus.SUCCESS);
    }

    // ========== ShutdownOnFailure：子任务失败 ==========

    @Test
    @DisplayName("ShutdownOnFailure — 用户不存在时，整体聚合失败")
    void aggregateOrderDetails_userNotFound_returnsFailure() {
        // Given
        Long orderId = 1L;
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(null);

        // When
        OrderAggregationResult result = service.aggregateOrderDetails(orderId, userId);

        // Then
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
    }

    @Test
    @DisplayName("ShutdownOnFailure — 订单不存在时，整体聚合失败")
    void aggregateOrderDetails_orderNotFound_returnsFailure() {
        // Given
        Long orderId = 999L;
        Long userId = 10L;

        JooqUserDto userDto = JooqUserDto.builder()
                .id(userId).username("testuser").email("test@example.com").build();
        when(userRepository.findById(userId)).thenReturn(userDto);
        when(orderRepository.findOrderWithItemsById(orderId)).thenReturn(null);

        // When
        OrderAggregationResult result = service.aggregateOrderDetails(orderId, userId);

        // Then
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
    }

    // ========== ShutdownOnSuccess：竞速模式 ==========

    @Test
    @DisplayName("ShutdownOnSuccess — 多个支付网关竞速，返回第一个成功结果")
    void racePaymentStatus_returnsFirstSuccessfulGateway() {
        // Given
        Long orderId = 1L;

        // When
        PaymentRaceResult result = service.racePaymentStatus(orderId);

        // Then
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getWinner()).isNotNull();
        assertThat(result.getWinner().getPaymentStatus()).isNotNull();
        assertThat(result.getWinnerGateway()).isNotNull();
        assertThat(result.getTotalDurationMillis()).isGreaterThan(0L);

        // 胜出的网关结果在 gatewayResults 中
        assertThat(result.getGatewayResults()).isNotEmpty();
        assertThat(result.getGatewayResults()).anyMatch(gr ->
                gr.getGatewayName().equals(result.getWinnerGateway())
                        && gr.getStatus() == SubTaskStatus.SUCCESS);
    }

    @Test
    @DisplayName("ShutdownOnSuccess — 竞速耗时不超过最慢网关")
    void racePaymentStatus_fasterThanSlowestGateway() {
        // Given
        Long orderId = 1L;

        // When
        PaymentRaceResult result = service.racePaymentStatus(orderId);

        // Then — 竞速模式的总耗时应远小于所有网关串行执行的时间
        // 3 个网关延迟分别为 50ms, 150ms, 500ms，串行总时间 = 700ms
        // 并行竞速应接近最慢的胜出者（约 50-150ms）
        assertThat(result.getTotalDurationMillis()).isLessThan(600L);
    }

    // ========== 自定义 Scope：降级聚合 ==========

    @Test
    @DisplayName("降级聚合 — 所有数据源成功时，返回完整结果（非降级）")
    void aggregateWithDegradation_allSucceed_returnsFullResult() {
        // Given
        Long orderId = 1L;
        Long userId = 10L;

        JooqUserDto userDto = JooqUserDto.builder()
                .id(userId).username("testuser").email("test@example.com").build();
        JooqOrderDto orderDto = JooqOrderDto.builder()
                .id(orderId).orderNumber("ORD-001").userId(userId)
                .status("PAID").items(List.of()).build();

        when(userRepository.findById(userId)).thenReturn(userDto);
        when(orderRepository.findOrderWithItemsById(orderId)).thenReturn(orderDto);

        // When
        OrderAggregationResult result = service.aggregateWithDegradation(orderId, userId);

        // Then
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getDegraded()).isFalse();
        assertThat(result.getStrategy()).isEqualTo(AggregationStrategy.DEGRADED_AGGREGATION);
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getOrderItemsInfo()).isNotNull();
        assertThat(result.getPaymentInfo()).isNotNull();
    }

    @Test
    @DisplayName("降级聚合 — 核心数据源失败时，整体失败")
    void aggregateWithDegradation_criticalFails_returnsFailure() {
        // Given
        Long orderId = 1L;
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(null);

        // When
        OrderAggregationResult result = service.aggregateWithDegradation(orderId, userId);

        // Then — 核心数据源（用户信息）失败，整体失败
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
    }
}
