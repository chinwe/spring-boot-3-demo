package com.example.demo.controller.orika;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.Address;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Order.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Orika 控制器集成测试
 *
 * 测试覆盖：
 * 1. 基础映射 API
 * 2. 复杂对象映射 API
 * 3. 集合映射 API
 * 4. 批量映射 API
 *
 * @author chinwe
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Orika 控制器集成测试")
class OrikaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
    }

    @Nested
    @DisplayName("基础映射 API 测试")
    class BasicMappingApiTests {

        @Test
        @DisplayName("GET /orika/user - 基础用户映射")
        void testGetUserMapping() throws Exception {
            mockMvc.perform(get("/orika/user"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.name").value("张三"))
                .andExpect(jsonPath("$.email").value("zhangsan@example.com"));
        }
    }

    @Nested
    @DisplayName("复杂映射 API 测试")
    class ComplexMappingApiTests {

        @Test
        @DisplayName("GET /orika/order - 复杂订单映射")
        void testGetOrderMapping() throws Exception {
            mockMvc.perform(get("/orika/order"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.customerName").exists())
                .andExpect(jsonPath("$.customerEmail").exists())
                .andExpect(jsonPath("$.orderDateStr").exists())
                .andExpect(jsonPath("$.statusDisplay").exists())
                .andExpect(jsonPath("$.mappedBy").value("Orika"))
                .andExpect(jsonPath("$.checksum").exists());
        }

        @Test
        @DisplayName("验证嵌套对象映射 - Customer 信息")
        void testNestedCustomerMapping() throws Exception {
            mockMvc.perform(get("/orika/order"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").exists())
                .andExpect(jsonPath("$.customerEmail").exists())
                .andExpect(jsonPath("$.customerAddress").exists())
                .andExpect(jsonPath("$.customerAddress.city").exists());
        }
    }

    @Nested
    @DisplayName("集合映射 API 测试")
    class CollectionMappingApiTests {

        @Test
        @DisplayName("GET /orika/orders?count=3 - 订单列表映射")
        void testGetOrdersMapping() throws Exception {
            mockMvc.perform(get("/orika/orders")
                    .param("count", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].orderNumber").exists())
                .andExpect(jsonPath("$[0].mappedBy").value("Orika"))
                .andExpect(jsonPath("$[1].mappedBy").value("Orika"))
                .andExpect(jsonPath("$[2].mappedBy").value("Orika"));
        }

        @Test
        @DisplayName("GET /orika/orders?count=5 - 自定义数量")
        void testGetOrdersWithCustomCount() throws Exception {
            mockMvc.perform(get("/orika/orders")
                    .param("count", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
        }

        @Test
        @DisplayName("GET /orika/orders - 默认数量")
        void testGetOrdersWithDefaultCount() throws Exception {
            mockMvc.perform(get("/orika/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
        }
    }

    @Nested
    @DisplayName("批量映射 API 测试")
    class BatchMappingApiTests {

        @Test
        @DisplayName("POST /orika/batch - 批量订单映射")
        void testBatchMapping() throws Exception {
            // Given
            List<Order> orders = createTestOrders(5);
            String jsonContent = objectMapper.writeValueAsString(orders);

            // When & Then
            mockMvc.perform(post("/orika/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].mappedBy").value("Orika"))
                .andExpect(jsonPath("$[4].mappedBy").value("Orika"));
        }

        @Test
        @DisplayName("POST /orika/batch - 空列表")
        void testBatchMappingWithEmptyList() throws Exception {
            // Given
            List<Order> orders = new ArrayList<>();
            String jsonContent = objectMapper.writeValueAsString(orders);

            // When & Then
            mockMvc.perform(post("/orika/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("POST /orika/batch - 单个订单")
        void testBatchMappingWithSingleOrder() throws Exception {
            // Given
            List<Order> orders = List.of(testOrder);
            String jsonContent = objectMapper.writeValueAsString(orders);

            // When & Then
            mockMvc.perform(post("/orika/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value(testOrder.getOrderNumber()));
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试 Order 对象
     */
    private Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("TEST-001");
        order.setOrderDate(LocalDateTime.of(2025, 1, 10, 14, 30));
        order.setStatus(OrderStatus.PROCESSING);
        order.setRemarks("测试订单");
        order.setTotalAmount(2999.99);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 创建 Customer
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("李");
        customer.setLastName("明");
        customer.setEmail("liming@example.com");
        customer.setPhone("13800138000");

        // 创建 Address
        Address address = new Address();
        address.setStreet("中关村大街1号");
        address.setCity("北京");
        address.setState("北京市");
        address.setZipCode("100080");
        address.setCountry("中国");

        customer.setAddress(address);
        order.setCustomer(customer);

        // 创建 OrderItems
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductName("笔记本电脑");
        item1.setQuantity(1);
        item1.setUnitPrice(5999.99);

        order.addItem(item1);

        return order;
    }

    /**
     * 创建测试 Order 列表
     */
    private List<Order> createTestOrders(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setOrderNumber("TEST-" + String.format("%03d", i + 1));
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.values()[i % OrderStatus.values().length]);
            order.setRemarks("测试订单 " + (i + 1));
            order.setTotalAmount(100.0 + i * 50);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // 创建简化的 Customer
            Customer customer = new Customer();
            customer.setId((long) (i + 1));
            customer.setFirstName("用户");
            customer.setLastName(String.valueOf(i + 1));
            customer.setEmail("user" + (i + 1) + "@example.com");
            customer.setPhone("1380013800" + (i % 10));
            order.setCustomer(customer);

            orders.add(order);
        }
        return orders;
    }
}
