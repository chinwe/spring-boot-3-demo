package com.example.demo.service.jooq;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqOrderRepository;

/**
 * JooqOrderService 单元测试
 * 验证订单服务的功能正确性
 *
 * @author chinwe
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JOOQ 订单服务单元测试")
class JooqOrderServiceTest {

    @Mock
    private JooqOrderRepository orderRepository;

    @InjectMocks
    private JooqOrderService orderService;

    private JooqOrderDto testOrder;
    private JooqOrderItemDto testOrderItem;

    @BeforeEach
    void setUp() {
        testOrderItem = JooqOrderItemDto.builder()
            .id(1L)
            .orderId(1L)
            .productId(100L)
            .quantity(2)
            .price(new BigDecimal("50.00"))
            .subtotal(new BigDecimal("100.00"))
            .build();

        testOrder = JooqOrderDto.builder()
            .id(1L)
            .orderNumber("ORD123456")
            .userId(10L)
            .username("testuser")
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .remarks("Test remarks")
            .items(Arrays.asList(testOrderItem))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("成功创建订单")
    void testCreateOrder_Success() {
        // Given
        when(orderRepository.insertWithItems(testOrder)).thenReturn(1L);

        // When
        Long resultId = orderService.createOrder(testOrder);

        // Then
        assertEquals(1L, resultId);
        verify(orderRepository, times(1)).insertWithItems(testOrder);
    }

    @Test
    @DisplayName("创建包含订单项的订单")
    void testCreateOrder_WithItems() {
        // Given
        JooqOrderItemDto item1 = JooqOrderItemDto.builder()
            .productId(100L)
            .quantity(1)
            .price(new BigDecimal("50.00"))
            .subtotal(new BigDecimal("50.00"))
            .build();

        JooqOrderItemDto item2 = JooqOrderItemDto.builder()
            .productId(200L)
            .quantity(2)
            .price(new BigDecimal("30.00"))
            .subtotal(new BigDecimal("60.00"))
            .build();

        JooqOrderDto orderWithItems = JooqOrderDto.builder()
            .orderNumber("ORD123")
            .userId(10L)
            .totalAmount(new BigDecimal("110.00"))
            .items(Arrays.asList(item1, item2))
            .build();

        when(orderRepository.insertWithItems(orderWithItems)).thenReturn(2L);

        // When
        Long resultId = orderService.createOrder(orderWithItems);

        // Then
        assertEquals(2L, resultId);
        verify(orderRepository, times(1)).insertWithItems(orderWithItems);
    }

    @Test
    @DisplayName("根据ID找到订单")
    void testGetOrderById_Found() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findOrderWithUserAndItemsById(orderId)).thenReturn(testOrder);

        // When
        JooqOrderDto result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertEquals(testOrder.getUserId(), result.getUserId());
        verify(orderRepository, times(1)).findOrderWithUserAndItemsById(orderId);
    }

    @Test
    @DisplayName("订单不存在抛出异常")
    void testGetOrderById_NotFound_ThrowsException() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findOrderWithUserAndItemsById(orderId)).thenReturn(null);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            orderService.getOrderById(orderId);
        });

        assertTrue(exception.getMessage().contains("Order not found with id"));
        assertTrue(exception.getMessage().contains("999"));
        verify(orderRepository, times(1)).findOrderWithUserAndItemsById(orderId);
    }

    @Test
    @DisplayName("订单包含用户和订单项信息")
    void testGetOrderById_WithUserAndItems() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findOrderWithUserAndItemsById(orderId)).thenReturn(testOrder);

        // When
        JooqOrderDto result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUsername());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals(testOrderItem.getProductId(), result.getItems().get(0).getProductId());
        verify(orderRepository, times(1)).findOrderWithUserAndItemsById(orderId);
    }

    @Test
    @DisplayName("返回用户订单列表")
    void testGetOrdersByUserId_ReturnsList() {
        // Given
        Long userId = 10L;
        JooqOrderDto order1 = JooqOrderDto.builder()
            .id(1L)
            .orderNumber("ORD001")
            .userId(userId)
            .username("testuser")
            .totalAmount(new BigDecimal("50.00"))
            .items(Collections.emptyList())
            .build();

        JooqOrderDto order2 = JooqOrderDto.builder()
            .id(2L)
            .orderNumber("ORD002")
            .userId(userId)
            .username("testuser")
            .totalAmount(new BigDecimal("75.00"))
            .items(Collections.emptyList())
            .build();

        List<JooqOrderDto> expectedOrders = Arrays.asList(order1, order2);
        when(orderRepository.findOrdersByUserId(userId)).thenReturn(expectedOrders);

        // When
        List<JooqOrderDto> result = orderService.getOrdersByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ORD001", result.get(0).getOrderNumber());
        assertEquals("ORD002", result.get(1).getOrderNumber());
        verify(orderRepository, times(1)).findOrdersByUserId(userId);
    }

    @Test
    @DisplayName("用户无订单返回空列表")
    void testGetOrdersByUserId_EmptyList() {
        // Given
        Long userId = 999L;
        when(orderRepository.findOrdersByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<JooqOrderDto> result = orderService.getOrdersByUserId(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findOrdersByUserId(userId);
    }

    @Test
    @DisplayName("返回统计信息")
    void testGetOrderStatistics_ReturnsMap() {
        // Given
        Map<String, Object> expectedStats = Map.of(
            "total_orders", 100L,
            "total_amount", new BigDecimal("10000.00"),
            "status_counts", Map.of(
                "PENDING", 50L,
                "COMPLETED", 40L,
                "CANCELLED", 10L
            )
        );
        when(orderRepository.getOrderStatistics()).thenReturn(expectedStats);

        // When
        Map<String, Object> result = orderService.getOrderStatistics();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("total_orders"));
        assertEquals(new BigDecimal("10000.00"), result.get("total_amount"));
        assertTrue(result.containsKey("status_counts"));
        verify(orderRepository, times(1)).getOrderStatistics();
    }

    @Test
    @DisplayName("验证统计信息包含预期字段")
    void testGetOrderStatistics_ContainsExpectedKeys() {
        // Given
        Map<String, Object> expectedStats = Map.of(
            "total_orders", 50L,
            "total_amount", new BigDecimal("5000.00"),
            "status_counts", Map.of("PENDING", 50L)
        );
        when(orderRepository.getOrderStatistics()).thenReturn(expectedStats);

        // When
        Map<String, Object> result = orderService.getOrderStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("total_orders"));
        assertTrue(result.containsKey("total_amount"));
        assertTrue(result.containsKey("status_counts"));
    }

    @Test
    @DisplayName("有订单时的统计信息")
    void testGetOrderStatistics_WithOrders() {
        // Given
        Map<String, Object> statsWithOrders = Map.of(
            "total_orders", 10L,
            "total_amount", new BigDecimal("999.99"),
            "status_counts", Map.of(
                "PENDING", 5L,
                "SHIPPED", 3L,
                "DELIVERED", 2L
            )
        );
        when(orderRepository.getOrderStatistics()).thenReturn(statsWithOrders);

        // When
        Map<String, Object> result = orderService.getOrderStatistics();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.get("total_orders"));
        assertEquals(new BigDecimal("999.99"), result.get("total_amount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> statusCounts = (Map<String, Object>) result.get("status_counts");
        assertNotNull(statusCounts);
        assertEquals(3, statusCounts.size());
    }
}
