package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqCreateOrderRequest;
import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqProductRepository;
import com.example.demo.repository.jooq.JooqUserRepository;
import com.example.demo.service.jooq.JooqTransactionService;

/**
 * JOOQ 集成测试
 * 测试完整的事务流程
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqIntegrationTest {

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

    /**
     * 测试完整的订单创建流程
     */
    @Test
    void testCompleteOrderFlow() {
        // 1. 创建用户
        JooqUserDto user = JooqUserDto.builder()
            .username("john")
            .email("john@example.com")
            .build();
        Long userId = userRepository.insert(user);
        assertNotNull(userId);

        // 2. 创建商品
        Long productId1 = productRepository.insert(
            JooqProductDto.builder()
                .name("Laptop")
                .description("High-end laptop")
                .price(new BigDecimal("1000.00"))
                .stock(10)
                .category("Electronics")
                .build()
        );
        assertNotNull(productId1);

        Long productId2 = productRepository.insert(
            JooqProductDto.builder()
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("20.00"))
                .stock(50)
                .category("Electronics")
                .build()
        );
        assertNotNull(productId2);

        // 3. 创建订单
        JooqCreateOrderRequest orderRequest = new JooqCreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId1, 1),
            new JooqCreateOrderRequest.OrderItemRequest(productId2, 2)
        ));
        orderRequest.setRemarks("Please ship quickly");

        Long orderId = transactionService.createOrder(orderRequest);
        assertNotNull(orderId);

        // 4. 验证订单创建
        JooqOrderDto order = orderRepository.findOrderWithUserAndItemsById(orderId);
        assertNotNull(order);
        assertEquals(userId, order.getUserId());
        assertEquals("john", order.getUsername());
        assertEquals(new BigDecimal("1040.00"), order.getTotalAmount());
        assertNotNull(order.getItems());
        assertEquals(2, order.getItems().size());

        // 5. 验证库存扣减
        JooqProductDto laptop = productRepository.findById(productId1);
        JooqProductDto mouse = productRepository.findById(productId2);
        assertEquals(9, laptop.getStock()); // 原来是10，买了1个
        assertEquals(48, mouse.getStock()); // 原来是50，买了2个
    }

    /**
     * 测试事务回滚
     * 当库存不足时，订单创建失败，库存应该不被扣减
     */
    @Test
    void testTransactionRollback() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        // 商品库存只有5个
        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Product")
                .price(new BigDecimal("100.00"))
                .stock(5)
                .build()
        );

        // When - 尝试购买10个，应该失败
        JooqCreateOrderRequest orderRequest = new JooqCreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 10)
        ));

        // Then - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createOrder(orderRequest);
        });

        // 验证库存没有被扣减（事务回滚）
        JooqProductDto product = productRepository.findById(productId);
        assertEquals(5, product.getStock());
    }
}
