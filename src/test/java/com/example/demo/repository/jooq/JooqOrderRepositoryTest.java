package com.example.demo.repository.jooq;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;
import com.example.demo.dto.jooq.JooqUserDto;

/**
 * JooqOrderRepository 测试类
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqOrderRepositoryTest {

    @Autowired
    private DSLContext dsl;

    private JooqOrderRepository orderRepository;
    private JooqUserRepository userRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new JooqOrderRepository(dsl);
        userRepository = new JooqUserRepository(dsl);
    }

    @Test
    void testInsertOrderWithItems() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build());

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("300.00"))
            .status("PENDING")
            .remarks("Test order")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(1L)
                    .quantity(2)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("200.00"))
                    .build(),
                JooqOrderItemDto.builder()
                    .productId(2L)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build()
            ))
            .build();

        // When
        Long orderId = orderRepository.insertWithItems(order);

        // Then
        assertNotNull(orderId);
        assertTrue(orderId > 0);
    }

    @Test
    void testFindOrderWithItemsById() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build());

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("200.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(1L)
                    .quantity(1)
                    .price(new BigDecimal("200.00"))
                    .subtotal(new BigDecimal("200.00"))
                    .build()
            ))
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        // When
        JooqOrderDto found = orderRepository.findOrderWithItemsById(orderId);

        // Then
        assertNotNull(found);
        assertEquals("ORD001", found.getOrderNumber());
        assertNotNull(found.getItems());
        assertEquals(1, found.getItems().size());
        assertEquals(1L, found.getItems().get(0).getProductId());
    }

    @Test
    void testFindOrderWithUserById() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("john_doe")
            .email("john@example.com")
            .build());

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD002")
            .userId(userId)
            .totalAmount(new BigDecimal("150.00"))
            .status("PENDING")
            .items(List.of())
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        // When
        JooqOrderDto found = orderRepository.findOrderWithUserById(orderId);

        // Then
        assertNotNull(found);
        assertEquals("ORD002", found.getOrderNumber());
        assertEquals("john_doe", found.getUsername());
    }

    @Test
    void testFindOrderWithUserAndItemsById() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("alice")
            .email("alice@example.com")
            .build());

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD003")
            .userId(userId)
            .totalAmount(new BigDecimal("500.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(1L)
                    .quantity(2)
                    .price(new BigDecimal("150.00"))
                    .subtotal(new BigDecimal("300.00"))
                    .build(),
                JooqOrderItemDto.builder()
                    .productId(2L)
                    .quantity(1)
                    .price(new BigDecimal("200.00"))
                    .subtotal(new BigDecimal("200.00"))
                    .build()
            ))
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        // When
        JooqOrderDto found = orderRepository.findOrderWithUserAndItemsById(orderId);

        // Then
        assertNotNull(found);
        assertEquals("ORD003", found.getOrderNumber());
        assertEquals("alice", found.getUsername());
        assertNotNull(found.getItems());
        assertEquals(2, found.getItems().size());
    }

    @Test
    void testFindOrdersByUserId() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("bob")
            .email("bob@example.com")
            .build());

        orderRepository.insertWithItems(JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of())
            .build());

        orderRepository.insertWithItems(JooqOrderDto.builder()
            .orderNumber("ORD002")
            .userId(userId)
            .totalAmount(new BigDecimal("200.00"))
            .status("COMPLETED")
            .items(List.of())
            .build());

        // When
        List<JooqOrderDto> orders = orderRepository.findOrdersByUserId(userId);

        // Then
        assertNotNull(orders);
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getUserId().equals(userId)));
    }

    @Test
    void testGetOrderStatistics() {
        // Given
        Long userId1 = userRepository.insert(JooqUserDto.builder()
            .username("user1")
            .email("user1@example.com")
            .build());

        Long userId2 = userRepository.insert(JooqUserDto.builder()
            .username("user2")
            .email("user2@example.com")
            .build());

        orderRepository.insertWithItems(JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId1)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of())
            .build());

        orderRepository.insertWithItems(JooqOrderDto.builder()
            .orderNumber("ORD002")
            .userId(userId1)
            .totalAmount(new BigDecimal("200.00"))
            .status("COMPLETED")
            .items(List.of())
            .build());

        orderRepository.insertWithItems(JooqOrderDto.builder()
            .orderNumber("ORD003")
            .userId(userId2)
            .totalAmount(new BigDecimal("300.00"))
            .status("COMPLETED")
            .items(List.of())
            .build());

        // When
        Map<String, Object> stats = orderRepository.getOrderStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("total_orders"));
        assertTrue(stats.containsKey("total_amount"));
        assertTrue(stats.containsKey("status_counts"));
    }

    @Test
    void testFindOrderWithItemsByIdNotFound() {
        // When
        JooqOrderDto found = orderRepository.findOrderWithItemsById(99999L);

        // Then
        assertNull(found);
    }

    @Test
    void testInsertOrderWithEmptyItems() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build());

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD004")
            .userId(userId)
            .totalAmount(BigDecimal.ZERO)
            .status("PENDING")
            .items(List.of())
            .build();

        // When
        Long orderId = orderRepository.insertWithItems(order);

        // Then
        assertNotNull(orderId);
        assertTrue(orderId > 0);
    }

    @Test
    void testInsertNullOrder() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            orderRepository.insertWithItems(null);
        });
    }

    @Test
    void testInsertOrderWithNullItems() {
        // Given
        Long userId = userRepository.insert(JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build());

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD005")
            .userId(userId)
            .totalAmount(BigDecimal.ZERO)
            .status("PENDING")
            .items(null)
            .build();

        // When
        Long orderId = orderRepository.insertWithItems(order);

        // Then
        assertNotNull(orderId);
        assertTrue(orderId > 0);
    }
}
