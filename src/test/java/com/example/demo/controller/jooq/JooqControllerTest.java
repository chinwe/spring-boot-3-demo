package com.example.demo.controller.jooq;

import com.example.demo.dto.jooq.*;
import com.example.demo.service.jooq.JooqOrderService;
import com.example.demo.service.jooq.JooqProductService;
import com.example.demo.service.jooq.JooqTransactionService;
import com.example.demo.service.jooq.JooqUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JooqController 单元测试
 * 覆盖所有 JOOQ 电商系统接口
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

    // ==================== 用户相关测试 ====================

    @Nested
    @DisplayName("用户接口测试")
    class UserEndpoints {

        @Test
        @DisplayName("POST /api/jooq/users — 创建用户")
        void testCreateUser() throws Exception {
            // Given
            JooqUserDto userDto = JooqUserDto.builder()
                    .username("newuser")
                    .email("newuser@example.com")
                    .phone("13800001111")
                    .build();
            when(userService.createUser(any(JooqUserDto.class))).thenReturn(1L);

            // When & Then
            mockMvc.perform(post("/api/jooq/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("用户创建成功，ID: 1"));

            verify(userService).createUser(any(JooqUserDto.class));
        }

        @Test
        @DisplayName("GET /api/jooq/users/{id} — 查询用户")
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

        @Test
        @DisplayName("GET /api/jooq/users — 查询所有用户")
        void testGetAllUsers() throws Exception {
            // Given
            JooqUserDto user1 = JooqUserDto.builder().id(1L).username("user1").email("u1@test.com").build();
            JooqUserDto user2 = JooqUserDto.builder().id(2L).username("user2").email("u2@test.com").build();
            when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

            // When & Then
            mockMvc.perform(get("/api/jooq/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].username").value("user1"))
                    .andExpect(jsonPath("$[1].username").value("user2"));
        }

        @Test
        @DisplayName("GET /api/jooq/users/username/{username} — 按用户名查询")
        void testGetUserByUsername() throws Exception {
            // Given
            JooqUserDto user = JooqUserDto.builder()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .build();
            when(userService.getUserByUsername("testuser")).thenReturn(user);

            // When & Then
            mockMvc.perform(get("/api/jooq/users/username/testuser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"));
        }
    }

    // ==================== 商品相关测试 ====================

    @Nested
    @DisplayName("商品接口测试")
    class ProductEndpoints {

        @Test
        @DisplayName("POST /api/jooq/products — 创建商品")
        void testCreateProduct() throws Exception {
            // Given
            JooqCreateProductRequest request = JooqCreateProductRequest.builder()
                    .name("Widget")
                    .description("A useful widget")
                    .price(new BigDecimal("29.99"))
                    .stock(100)
                    .category("tools")
                    .build();
            when(productService.createProduct(any(JooqCreateProductRequest.class))).thenReturn(1L);

            // When & Then
            mockMvc.perform(post("/api/jooq/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("商品创建成功，ID: 1"));
        }

        @Test
        @DisplayName("POST /api/jooq/products/batch — 批量创建商品")
        void testBatchCreateProducts() throws Exception {
            // Given
            JooqCreateProductRequest req1 = JooqCreateProductRequest.builder()
                    .name("Product A").price(new BigDecimal("10.00")).stock(50).build();
            JooqCreateProductRequest req2 = JooqCreateProductRequest.builder()
                    .name("Product B").price(new BigDecimal("20.00")).stock(30).build();

            // When & Then
            mockMvc.perform(post("/api/jooq/products/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(req1, req2))))
                    .andExpect(status().isOk())
                    .andExpect(content().string("批量创建商品成功，数量: 2"));

            verify(productService).batchCreateProducts(anyList());
        }

        @Test
        @DisplayName("GET /api/jooq/products/{id} — 查询商品")
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

        @Test
        @DisplayName("GET /api/jooq/products — 按分类查询商品（分页）")
        void testGetProductsByCategory() throws Exception {
            // Given
            JooqProductDto p1 = JooqProductDto.builder().id(1L).name("P1").category("electronics").build();
            JooqProductDto p2 = JooqProductDto.builder().id(2L).name("P2").category("electronics").build();
            when(productService.getProductsByCategory(eq("electronics"), eq(0), eq(10)))
                    .thenReturn(List.of(p1, p2));

            // When & Then
            mockMvc.perform(get("/api/jooq/products")
                            .param("category", "electronics")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].category").value("electronics"));
        }

        @Test
        @DisplayName("GET /api/jooq/products/low-stock — 查询低库存商品")
        void testGetLowStockProducts() throws Exception {
            // Given
            JooqProductDto lowStock = JooqProductDto.builder().id(1L).name("LowStock").stock(3).build();
            when(productService.getLowStockProducts(10)).thenReturn(List.of(lowStock));

            // When & Then
            mockMvc.perform(get("/api/jooq/products/low-stock")
                            .param("threshold", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].stock").value(3));
        }

        @Test
        @DisplayName("GET /api/jooq/products/stock/{category} — 查询分类库存统计")
        void testGetTotalStock() throws Exception {
            // Given
            when(productService.getTotalStockByCategory("electronics")).thenReturn(150);

            // When & Then
            mockMvc.perform(get("/api/jooq/products/stock/electronics"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("分类 [electronics] 总库存: 150"));
        }
    }

    // ==================== 订单相关测试 ====================

    @Nested
    @DisplayName("订单接口测试")
    class OrderEndpoints {

        @Test
        @DisplayName("POST /api/jooq/orders — 创建订单")
        void testCreateOrder() throws Exception {
            // Given
            JooqCreateOrderRequest.OrderItemRequest item = JooqCreateOrderRequest.OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build();
            JooqCreateOrderRequest request = JooqCreateOrderRequest.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .remarks("Test order")
                    .build();
            when(transactionService.createOrder(any(JooqCreateOrderRequest.class))).thenReturn(100L);

            // When & Then
            mockMvc.perform(post("/api/jooq/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("订单创建成功，订单ID: 100"));
        }

        @Test
        @DisplayName("GET /api/jooq/orders/{id} — 查询订单详情")
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

        @Test
        @DisplayName("GET /api/jooq/users/{userId}/orders — 查询用户订单")
        void testGetUserOrders() throws Exception {
            // Given
            JooqOrderDto order1 = JooqOrderDto.builder().id(1L).orderNumber("ORD001").userId(1L).build();
            JooqOrderDto order2 = JooqOrderDto.builder().id(2L).orderNumber("ORD002").userId(1L).build();
            when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(order1, order2));

            // When & Then
            mockMvc.perform(get("/api/jooq/users/1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].orderNumber").value("ORD001"))
                    .andExpect(jsonPath("$[1].orderNumber").value("ORD002"));
        }

        @Test
        @DisplayName("GET /api/jooq/orders/statistics — 订单统计")
        void testGetOrderStatistics() throws Exception {
            // Given
            Map<String, Object> stats = Map.of(
                    "totalOrders", 10,
                    "totalAmount", new BigDecimal("5000.00")
            );
            when(orderService.getOrderStatistics()).thenReturn(stats);

            // When & Then
            mockMvc.perform(get("/api/jooq/orders/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(10));
        }
    }
}
