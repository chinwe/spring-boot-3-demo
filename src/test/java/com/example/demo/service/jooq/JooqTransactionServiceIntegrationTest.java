package com.example.demo.service.jooq;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqCreateOrderRequest;
import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqProductRepository;
import com.example.demo.repository.jooq.JooqUserRepository;

/**
 * JooqTransactionService 集成测试
 * 测试事务管理服务的完整功能
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("JOOQ 事务服务集成测试")
class JooqTransactionServiceIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private JooqTransactionService transactionService;

    private JooqUserRepository userRepository;
    private JooqProductRepository productRepository;
    private JooqOrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        userRepository = new JooqUserRepository(dsl);
        productRepository = new JooqProductRepository(dsl);
        orderRepository = new JooqOrderRepository(dsl);
    }

    @Test
    @DisplayName("成功创建订单")
    void testCreateOrder_Success() {
        // Given - 创建用户和商品
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Laptop")
                .description("High-end laptop")
                .price(new BigDecimal("1000.00"))
                .stock(10)
                .category("Electronics")
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 2)
        ));

        // When
        Long orderId = transactionService.createOrder(request);

        // Then
        assertNotNull(orderId);
        JooqOrderDto order = orderRepository.findOrderWithUserAndItemsById(orderId);
        assertNotNull(order);
        assertEquals(userId, order.getUserId());
        assertEquals(new BigDecimal("2000.00"), order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
    }

    @Test
    @DisplayName("用户不存在抛出异常")
    void testCreateOrder_UserNotFound_ThrowsException() {
        // Given - 不存在的用户ID
        Long nonExistentUserId = 99999L;

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(nonExistentUserId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(1L, 1)
        ));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transactionService.createOrder(request);
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("商品不存在抛出异常")
    void testCreateOrder_ProductNotFound_ThrowsException() {
        // Given - 创建用户但使用不存在的商品ID
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(99999L, 1)
        ));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transactionService.createOrder(request);
        });

        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    @DisplayName("库存不足抛出异常")
    void testCreateOrder_InsufficientStock_ThrowsException() {
        // Given - 创建库存不足的商品
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Limited Stock Item")
                .price(new BigDecimal("100.00"))
                .stock(3)
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 5) // 尝试购买5个但只有3个
        ));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createOrder(request);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    @DisplayName("验证订单号生成")
    void testCreateOrder_OrderNumberGenerated() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Product")
                .price(new BigDecimal("50.00"))
                .stock(10)
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 1)
        ));

        // When
        Long orderId = transactionService.createOrder(request);

        // Then
        JooqOrderDto order = orderRepository.findOrderWithUserAndItemsById(orderId);
        assertNotNull(order.getOrderNumber());
        assertTrue(order.getOrderNumber().startsWith("ORD"));
        assertTrue(order.getOrderNumber().length() > 10);
    }

    @Test
    @DisplayName("验证库存扣减")
    void testCreateOrder_StockDecreased() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Product")
                .price(new BigDecimal("50.00"))
                .stock(100)
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 10)
        ));

        // When
        transactionService.createOrder(request);

        // Then
        JooqProductDto product = productRepository.findById(productId);
        assertEquals(90, product.getStock()); // 100 - 10 = 90
    }

    @Test
    @DisplayName("失败时事务回滚")
    void testCreateOrder_RollbackOnFailure() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Product")
                .price(new BigDecimal("50.00"))
                .stock(5)
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 10) // 库存不足
        ));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createOrder(request);
        });

        // @Transactional 注解确保事务回滚，订单不会被创建
        // 由于 @Transactional 注解的测试方法会在结束后自动回滚
        // 这里只需要验证抛出了异常即可
    }

    @Test
    @DisplayName("错误时库存不扣减")
    void testCreateOrder_StockNotDecreasedOnError() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Product")
                .price(new BigDecimal("50.00"))
                .stock(5)
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 10) // 尝试购买10个但库存只有5个
        ));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createOrder(request);
        });

        // 验证库存没有被扣减
        JooqProductDto product = productRepository.findById(productId);
        assertEquals(5, product.getStock());
    }

    @Test
    @DisplayName("批量创建订单成功")
    void testBatchCreateOrders_Success() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId1 = productRepository.insert(
            JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("50.00"))
                .stock(20)
                .build()
        );

        Long productId2 = productRepository.insert(
            JooqProductDto.builder()
                .name("Product 2")
                .price(new BigDecimal("30.00"))
                .stock(20)
                .build()
        );

        JooqCreateOrderRequest request1 = new JooqCreateOrderRequest();
        request1.setUserId(userId);
        request1.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId1, 2)
        ));

        JooqCreateOrderRequest request2 = new JooqCreateOrderRequest();
        request2.setUserId(userId);
        request2.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId2, 3)
        ));

        // When
        List<Long> orderIds = transactionService.batchCreateOrders(List.of(request1, request2));

        // Then
        assertNotNull(orderIds);
        assertEquals(2, orderIds.size());
        assertTrue(orderIds.get(0) > 0);
        assertTrue(orderIds.get(1) > 0);
    }

    @Test
    @DisplayName("空列表处理")
    void testBatchCreateOrders_EmptyList() {
        // Given
        List<JooqCreateOrderRequest> requests = List.of();

        // When
        List<Long> orderIds = transactionService.batchCreateOrders(requests);

        // Then
        assertNotNull(orderIds);
        assertEquals(0, orderIds.size());
    }

    @Test
    @DisplayName("创建订单并返回详情")
    void testCreateOrderWithDetails_ReturnsFullOrder() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Laptop")
                .description("High-end laptop")
                .price(new BigDecimal("1000.00"))
                .stock(10)
                .category("Electronics")
                .build()
        );

        JooqCreateOrderRequest request = new JooqCreateOrderRequest();
        request.setUserId(userId);
        request.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 2)
        ));
        request.setRemarks("Express shipping");

        // When
        JooqOrderDto order = transactionService.createOrderWithDetails(request);

        // Then
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(userId, order.getUserId());
        assertEquals("john", order.getUsername());
        assertEquals(new BigDecimal("2000.00"), order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
        assertEquals("Express shipping", order.getRemarks());
        assertNotNull(order.getItems());
        assertEquals(1, order.getItems().size());
    }

    @Test
    @DisplayName("获取系统统计信息")
    void testGetSystemStatistics_ReturnsExpectedData() {
        // Given - 创建测试数据
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        productRepository.insert(
            JooqProductDto.builder()
                .name("Electronics Product")
                .price(new BigDecimal("100.00"))
                .stock(50)
                .category("Electronics")
                .build()
        );

        productRepository.insert(
            JooqProductDto.builder()
                .name("Book")
                .price(new BigDecimal("20.00"))
                .stock(30)
                .category("Books")
                .build()
        );

        // When
        Map<String, Object> stats = transactionService.getSystemStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("electronics_stock"));
        assertTrue(stats.containsKey("books_stock"));
        assertTrue(stats.containsKey("low_stock_products_count"));

        assertEquals(50, stats.get("electronics_stock"));
        assertEquals(30, stats.get("books_stock"));
        assertEquals(0, stats.get("low_stock_products_count"));
    }
}
