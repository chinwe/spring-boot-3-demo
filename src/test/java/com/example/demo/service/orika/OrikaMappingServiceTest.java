package com.example.demo.service.orika;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.dto.OrderDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Address;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Order.OrderStatus;
import com.example.demo.entity.User;

/**
 * Orika 映射服务测试
 *
 * 测试覆盖：
 * 1. 基础对象映射（User -> UserDto）
 * 2. 复杂对象映射（Order -> OrderDto）
 * 3. 嵌套对象映射
 * 4. 集合映射
 * 5. 空值处理
 *
 * @author chinwe
 */
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Orika 映射服务测试")
class OrikaMappingServiceTest {

    @Resource
    private OrikaMappingService mappingService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User(1L, "张三", "zhangsan@example.com");

        testOrder = createTestOrder();
    }

    @Nested
    @DisplayName("基础映射测试")
    class BasicMappingTests {

        @Test
        @org.junit.jupiter.api.Order(1)
        @DisplayName("测试 User -> UserDto 映射")
        void testUserToUserDtoMapping() {
            // When
            UserDto dto = mappingService.toUserDto(testUser);

            // Then
            assertAll("UserDto mappings",
                () -> assertNotNull(dto, "DTO should not be null"),
                () -> assertEquals(testUser.getName(), dto.getName(), "Name should match"),
                () -> assertEquals(testUser.getEmail(), dto.getEmail(), "Email should match")
            );
        }

        @Test
        @org.junit.jupiter.api.Order(2)
        @DisplayName("测试 UserDto -> User 反向映射")
        void testUserDtoToUserMapping() {
            // Given
            UserDto dto = new UserDto();
            dto.setName("李四");
            dto.setEmail("lisi@example.com");

            // When
            User user = mappingService.toUser(dto);

            // Then
            assertAll("User mappings",
                () -> assertNotNull(user, "User should not be null"),
                () -> assertEquals(dto.getName(), user.getName(), "Name should match"),
                () -> assertEquals(dto.getEmail(), user.getEmail(), "Email should match")
            );
        }

        @Test
        @org.junit.jupiter.api.Order(3)
        @DisplayName("测试空值处理 - User 为 null")
        void testNullUserMapping() {
            // When
            UserDto dto = mappingService.toUserDto(null);

            // Then
            assertNull(dto, "Null user should result in null DTO");
        }

        @Test
        @org.junit.jupiter.api.Order(4)
        @DisplayName("测试空值处理 - UserDto 为 null")
        void testNullUserDtoMapping() {
            // When
            User user = mappingService.toUser(null);

            // Then
            assertNull(user, "Null DTO should result in null user");
        }
    }

    @Nested
    @DisplayName("复杂映射测试")
    class ComplexMappingTests {

        @Test
        @org.junit.jupiter.api.Order(1)
        @DisplayName("测试 Order -> OrderDto 映射（嵌套对象）")
        void testOrderToOrderDtoMapping() {
            // When
            OrderDto dto = mappingService.toOrderDto(testOrder);

            // Then
            assertAll("OrderDto mappings",
                () -> assertNotNull(dto, "DTO should not be null"),
                () -> assertEquals(testOrder.getId(), dto.getId(), "ID should match"),
                () -> assertEquals(testOrder.getOrderNumber(), dto.getOrderNumber(), "Order number should match"),
                () -> assertNotNull(dto.getOrderDateStr(), "Order date string should not be null"),
                () -> assertNotNull(dto.getStatusDisplay(), "Status display should not be null"),
                () -> assertEquals("Orika", dto.getMappedBy(), "Should be mapped by Orika"),
                () -> assertNotNull(dto.getChecksum(), "Checksum should be calculated")
            );
        }

        @Test
        @org.junit.jupiter.api.Order(2)
        @DisplayName("测试嵌套 Customer 对象映射")
        void testNestedCustomerMapping() {
            // When
            OrderDto dto = mappingService.toOrderDto(testOrder);

            // Then
            assertAll("Customer nested mappings",
                () -> assertNotNull(dto.getCustomerName(), "Customer name should not be null"),
                () -> assertNotNull(dto.getCustomerEmail(), "Customer email should not be null"),
                () -> assertEquals("李 明", dto.getCustomerName(), "Full name should match"),
                () -> assertEquals("liming@example.com", dto.getCustomerEmail(), "Email should match")
            );
        }

        @Test
        @org.junit.jupiter.api.Order(3)
        @DisplayName("测试嵌套 Address 对象映射")
        void testNestedAddressMapping() {
            // When
            OrderDto dto = mappingService.toOrderDto(testOrder);

            // Then
            assertAll("Address nested mappings",
                () -> assertNotNull(dto.getCustomerAddress(), "Customer address should not be null"),
                () -> assertNotNull(dto.getCustomerAddress().getCity(), "City should not be null"),
                () -> assertEquals("北京", dto.getCustomerAddress().getCity(), "City should match")
            );
        }

        @Test
        @org.junit.jupiter.api.Order(4)
        @DisplayName("测试自定义转换器 - 日期格式化")
        void testDateConversion() {
            // When
            OrderDto dto = mappingService.toOrderDto(testOrder);

            // Then
            assertNotNull(dto.getOrderDateStr(), "Date string should not be null");
            // 验证日期格式：yyyy-MM-dd HH:mm:ss
            assertEquals(19, dto.getOrderDateStr().length(), "Date string should have format length");
        }

        @Test
        @org.junit.jupiter.api.Order(5)
        @DisplayName("测试自定义转换器 - 枚举转换")
        void testEnumConversion() {
            // When
            OrderDto dto = mappingService.toOrderDto(testOrder);

            // Then
            assertEquals("Processing", dto.getStatusDisplay(), "Status should be formatted");
        }

        @Test
        @org.junit.jupiter.api.Order(6)
        @DisplayName("测试空值处理 - Order 为 null")
        void testNullOrderMapping() {
            // When
            OrderDto dto = mappingService.toOrderDto(null);

            // Then
            assertNull(dto, "Null order should result in null DTO");
        }
    }

    @Nested
    @DisplayName("集合映射测试")
    class CollectionMappingTests {

        @Test
        @org.junit.jupiter.api.Order(1)
        @DisplayName("测试 Order 列表映射")
        void testOrderListMapping() {
            // Given
            List<Order> orders = Arrays.asList(
                createTestOrder(),
                createTestOrder(),
                createTestOrder()
            );

            // When
            List<OrderDto> dtoList = mappingService.toOrderDtoList(orders);

            // Then
            assertAll("List mapping",
                () -> assertNotNull(dtoList, "DTO list should not be null"),
                () -> assertEquals(3, dtoList.size(), "List size should match")
            );

            // 验证每个 DTO 都有 mappedBy 标识
            dtoList.forEach(dto -> {
                assertEquals("Orika", dto.getMappedBy(), "Each DTO should be mapped by Orika");
                assertNotNull(dto.getChecksum(), "Each DTO should have checksum");
            });
        }

        @Test
        @org.junit.jupiter.api.Order(2)
        @DisplayName("测试空列表映射")
        void testEmptyListMapping() {
            // Given
            List<Order> orders = Arrays.asList();

            // When
            List<OrderDto> dtoList = mappingService.toOrderDtoList(orders);

            // Then
            assertNotNull(dtoList, "DTO list should not be null");
            assertEquals(0, dtoList.size(), "List should be empty");
        }

        @Test
        @org.junit.jupiter.api.Order(3)
        @DisplayName("测试 null 列表映射")
        void testNullListMapping() {
            // When
            List<OrderDto> dtoList = mappingService.toOrderDtoList(null);

            // Then
            assertNull(dtoList, "Null list should result in null DTO list");
        }

        @Test
        @org.junit.jupiter.api.Order(4)
        @DisplayName("测试批量映射性能")
        void testBatchMappingPerformance() {
            // Given
            List<Order> orders = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                orders.add(createTestOrder());
            }

            // When
            long startTime = System.currentTimeMillis();
            List<OrderDto> dtoList = mappingService.toOrderDtoListBatch(orders);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertAll("Batch mapping",
                () -> assertEquals(100, dtoList.size(), "All orders should be mapped"),
                () -> {
                    // 性能断言：100 个对象应在合理时间内完成（例如 5 秒）
                    if (duration > 5000) {
                        throw new AssertionError("Batch mapping took too long: " + duration + " ms");
                    }
                }
            );

            System.out.println("Mapped 100 orders in " + duration + " ms");
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
}
