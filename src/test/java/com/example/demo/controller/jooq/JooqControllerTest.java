package com.example.demo.controller.jooq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.service.jooq.JooqOrderService;
import com.example.demo.service.jooq.JooqProductService;
import com.example.demo.service.jooq.JooqTransactionService;
import com.example.demo.service.jooq.JooqUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JooqController 测试类
 *
 * @author chinwe
 */
@WebMvcTest(JooqController.class)
class JooqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JooqUserService userService;

    @MockitoBean
    private JooqProductService productService;

    @MockitoBean
    private JooqOrderService orderService;

    @MockitoBean
    private JooqTransactionService transactionService;

    /**
     * 测试获取用户信息
     */
    @Test
    void testGetUser() throws Exception {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        when(userService.getUserById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/jooq/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * 测试获取商品信息
     */
    @Test
    void testGetProduct() throws Exception {
        // Given
        JooqProductDto product = JooqProductDto.builder()
            .id(1L)
            .name("Product 1")
            .price(new BigDecimal("100.00"))
            .stock(10)
            .build();
        when(productService.getProductById(1L)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/jooq/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Product 1"))
            .andExpect(jsonPath("$.price").value(100.00));
    }

    /**
     * 测试获取订单信息
     */
    @Test
    void testGetOrder() throws Exception {
        // Given
        JooqOrderDto order = JooqOrderDto.builder()
            .id(1L)
            .orderNumber("ORD001")
            .userId(1L)
            .username("testuser")
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .build();
        when(orderService.getOrderById(1L)).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/jooq/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNumber").value("ORD001"))
            .andExpect(jsonPath("$.username").value("testuser"));
    }
}
