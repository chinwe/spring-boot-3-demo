package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderItemDto;
import com.example.demo.entity.Address;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;

/**
 * OrderMapper 完整功能测试
 * <p>
 *
 * 测试覆盖：
 * 1. 基本对象映射
 * 2. 嵌套对象映射
 * 3. 集合映射
 * 4. 自定义映射方法
 * 5. 生命周期回调
 * 6. 空值处理
 * 7. 默认值和常量
 * 8. 验证逻辑
 *
 * 
 * @author chinwe
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    private Order testOrder;
    private Customer testCustomer;
    private Address testAddress;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        // 创建测试地址
        testAddress = new Address();
        testAddress.setStreet("123 Main Street");
        testAddress.setCity("New York");
        testAddress.setState("NY");
        testAddress.setZipCode("10001");
        testAddress.setCountry("USA");

        // 创建测试客户
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhone("123-456-7890");
        testCustomer.setAddress(testAddress);

        // 创建测试订单项
        testItems = new ArrayList<>();

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductCode("PROD-001");
        item1.setProductName("Laptop");
        item1.setQuantity(2);
        item1.setUnitPrice(999.99);
        testItems.add(item1);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductCode("PROD-002");
        item2.setProductName("Mouse");
        item2.setQuantity(5);
        item2.setUnitPrice(29.99);
        testItems.add(item2);

        // 创建测试订单
        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setOrderNumber("ORD-2024-001");
        testOrder.setOrderDate(LocalDateTime.of(2024, 12, 31, 14, 30));
        testOrder.setStatus(Order.OrderStatus.PROCESSING);
        testOrder.setCustomer(testCustomer);
        testOrder.setItems(testItems);
        testOrder.setRemarks("Gift wrapping required");
        testOrder.setTotalAmount(2149.93);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("基本映射测试")
    class BasicMappingTests {

        @Test
        @org.junit.jupiter.api.Order(1)
        @DisplayName("测试简单对象映射 - Order to OrderDto")
        void testBasicMapping() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then
            assertAll("OrderDto mappings",
                () -> assertNotNull(dto),
                () -> assertEquals(testOrder.getId(), dto.getId()),
                () -> assertEquals(testOrder.getOrderNumber(), dto.getOrderNumber()),
                () -> assertNotNull(dto.getOrderDateStr()),
                () -> assertEquals("Processing", dto.getStatusDisplay()),
                () -> assertEquals("MapStruct", dto.getMappedBy()),
                () -> assertEquals("Gift wrapping required", dto.getRemarks())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(2)
        @DisplayName("测试嵌套对象映射 - Customer fields")
        void testNestedObjectMapping() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then
            assertAll("Nested customer mappings",
                () -> assertEquals("John Doe", dto.getCustomerName()),
                () -> assertEquals("john.doe@example.com", dto.getCustomerEmail())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(3)
        @DisplayName("测试集合映射 - Order items")
        void testCollectionMapping() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then
            assertAll("Collection mappings",
                () -> assertNotNull(dto.getItems()),
                () -> assertEquals(2, dto.getItems().size()),
                () -> assertEquals("PROD-001", dto.getItems().get(0).getProductCode()),
                () -> assertEquals("Laptop", dto.getItems().get(0).getProductName()),
                () -> assertEquals(2, dto.getItems().get(0).getQuantity()),
                () -> assertEquals("$999.99", dto.getItems().get(0).getUnitPriceDisplay())
            );
        }
    }

    @Nested
    @DisplayName("自定义方法测试")
    class CustomMethodTests {

        @Test
        @org.junit.jupiter.api.Order(4)
        @DisplayName("测试日期格式化")
        void testDateFormatting() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then
            assertNotNull(dto.getOrderDateStr());
            assertTrue(dto.getOrderDateStr().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        }

        @Test
        @org.junit.jupiter.api.Order(5)
        @DisplayName("测试货币格式化")
        void testCurrencyFormatting() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then
            assertEquals("$2149.93", dto.getTotalAmountDisplay());
        }

        @Test
        @org.junit.jupiter.api.Order(6)
        @DisplayName("测试状态显示转换")
        void testStatusDisplayConversion() {
            // Test various statuses - create independent order instances to avoid state pollution
            Order pendingOrder = createTestOrder(100L, "ORD-2024-001", Order.OrderStatus.PENDING);
            OrderDto dto1 = orderMapper.toOrderDto(pendingOrder);
            assertEquals("Pending", dto1.getStatusDisplay());

            Order shippedOrder = createTestOrder(101L, "ORD-2024-002", Order.OrderStatus.SHIPPED);
            OrderDto dto2 = orderMapper.toOrderDto(shippedOrder);
            assertEquals("Shipped", dto2.getStatusDisplay());

            Order deliveredOrder = createTestOrder(102L, "ORD-2024-003", Order.OrderStatus.DELIVERED);
            OrderDto dto3 = orderMapper.toOrderDto(deliveredOrder);
            assertEquals("Delivered", dto3.getStatusDisplay());
        }
    }

    @Nested
    @DisplayName("生命周期回调测试")
    class LifecycleCallbackTests {

        @Test
        @org.junit.jupiter.api.Order(7)
        @DisplayName("测试 @AfterMapping - enrichment")
        void testAfterMappingEnrichment() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then - checksum should be calculated by @AfterMapping
            assertNotNull(dto.getChecksum());
            assertNotNull(dto.getCreatedAtEpoch());
        }

        @Test
        @org.junit.jupiter.api.Order(8)
        @DisplayName("测试 @BeforeMapping - validation")
        void testBeforeMappingValidation() {
            // Given - order with null order number
            Order invalidOrder = new Order();
            invalidOrder.setOrderNumber(null);

            // When & Then - should throw exception
            assertThrows(IllegalArgumentException.class, () -> {
                orderMapper.toOrderDto(invalidOrder);
            });
        }
    }

    @Nested
    @DisplayName("空值和默认值测试")
    class NullAndDefaultTests {

        @Test
        @org.junit.jupiter.api.Order(9)
        @DisplayName("测试空值映射")
        void testNullMapping() {
            // When
            OrderDto dto = orderMapper.toOrderDto(null);

            // Then
            assertNull(dto);
        }

        @Test
        @org.junit.jupiter.api.Order(10)
        @DisplayName("测试默认值 - remarks")
        void testDefaultValueForRemarks() {
            // Given - order without remarks
            testOrder.setRemarks(null);

            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then - should use default value
            assertEquals("No remarks", dto.getRemarks());
        }

        @Test
        @org.junit.jupiter.api.Order(11)
        @DisplayName("测试空集合映射")
        void testEmptyCollectionMapping() {
            // Given - order with no items
            testOrder.setItems(new ArrayList<>());

            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then
            assertNotNull(dto.getItems());
            assertTrue(dto.getItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("集合映射测试")
    class ListMappingTests {

        @Test
        @org.junit.jupiter.api.Order(12)
        @DisplayName("测试订单列表映射")
        void testOrderListMapping() {
            // Given
            List<Order> orders = new ArrayList<>();
            orders.add(testOrder);

            Order order2 = new Order();
            order2.setId(101L);
            order2.setOrderNumber("ORD-2024-002");
            order2.setOrderDate(LocalDateTime.now());
            order2.setStatus(Order.OrderStatus.PENDING);
            order2.setCustomer(testCustomer);
            order2.setItems(new ArrayList<>());
            order2.setTotalAmount(0.0);
            orders.add(order2);

            // When
            List<OrderDto> dtos = orderMapper.toOrderDtoList(orders);

            // Then
            assertAll("List mapping",
                () -> assertNotNull(dtos),
                () -> assertEquals(2, dtos.size()),
                () -> assertEquals("ORD-2024-001", dtos.get(0).getOrderNumber()),
                () -> assertEquals("ORD-2024-002", dtos.get(1).getOrderNumber())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(13)
        @DisplayName("测试空列表映射")
        void testEmptyListMapping() {
            // When
            List<OrderDto> dtos = orderMapper.toOrderDtoList(new ArrayList<>());

            // Then
            assertNotNull(dtos);
            assertTrue(dtos.isEmpty());
        }

        @Test
        @org.junit.jupiter.api.Order(14)
        @DisplayName("测试 null 列表映射")
        void testNullListMapping() {
            // When
            List<OrderDto> dtos = orderMapper.toOrderDtoList(null);

            // Then
            assertNull(dtos);
        }
    }

    @Nested
    @DisplayName("AddressMapper 测试")
    class AddressMapperTests {

        @Test
        @org.junit.jupiter.api.Order(15)
        @DisplayName("测试地址映射")
        void testAddressMapping() {
            // When
            AddressDto dto = addressMapper.toAddressDto(testAddress);

            // Then
            assertAll("Address mapping",
                () -> assertNotNull(dto),
                () -> assertEquals("123 Main Street, New York, NY", dto.getFullAddress()),
                () -> assertEquals("New York", dto.getCity()),
                () -> assertEquals("USA", dto.getCountry()),
                () -> assertEquals("10001", dto.getPostalCode())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(16)
        @DisplayName("测试地址反向映射")
        void testAddressReverseMapping() {
            // Given
            AddressDto dto = new AddressDto();
            dto.setFullAddress("456 Oak Avenue, Los Angeles, CA");
            dto.setCity("Los Angeles");
            dto.setCountry("USA");
            dto.setPostalCode("90001");

            // When
            Address address = addressMapper.toAddress(dto);

            // Then
            assertAll("Address reverse mapping",
                () -> assertNotNull(address),
                () -> assertEquals("Los Angeles", address.getCity()),
                () -> assertEquals("USA", address.getCountry()),
                () -> assertEquals("90001", address.getZipCode())
            );
        }
    }

    @Nested
    @DisplayName("OrderItemMapper 测试")
    class OrderItemMapperTests {

        @Test
        @org.junit.jupiter.api.Order(17)
        @DisplayName("测试订单项映射")
        void testOrderItemMapping() {
            // Given
            OrderItem item = testItems.get(0);

            // When
            OrderItemDto dto = orderItemMapper.toOrderItemDto(item);

            // Then
            assertAll("OrderItem mapping",
                () -> assertNotNull(dto),
                () -> assertEquals("PROD-001", dto.getProductCode()),
                () -> assertEquals("Laptop", dto.getProductName()),
                () -> assertEquals(2, dto.getQuantity()),
                () -> assertEquals("$999.99", dto.getUnitPriceDisplay()),
                () -> assertEquals("$1999.98", dto.getSubtotalDisplay())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(18)
        @DisplayName("测试订单项列表映射")
        void testOrderItemListMapping() {
            // When
            List<OrderItemDto> dtos = orderItemMapper.toOrderItemDtoList(testItems);

            // Then
            assertAll("OrderItem list mapping",
                () -> assertNotNull(dtos),
                () -> assertEquals(2, dtos.size()),
                () -> assertEquals("$1999.98", dtos.get(0).getSubtotalDisplay()),
                () -> assertEquals("$149.95", dtos.get(1).getSubtotalDisplay())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(19)
        @DisplayName("测试货币解析")
        void testCurrencyParsing() {
            // Given
            OrderItemDto dto = new OrderItemDto();
            dto.setProductCode("TEST-001");
            dto.setProductName("Test Product");
            dto.setQuantity(1);
            dto.setUnitPriceDisplay("$99.99");
            dto.setSubtotalDisplay("$99.99");

            // When
            OrderItem item = orderItemMapper.toOrderItem(dto);

            // Then
            assertAll("Currency parsing",
                () -> assertNotNull(item),
                () -> assertEquals(99.99, item.getUnitPrice())
            );
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @org.junit.jupiter.api.Order(20)
        @DisplayName("测试完整映射流程")
        void testCompleteMappingFlow() {
            // When
            OrderDto dto = orderMapper.toOrderDto(testOrder);

            // Then - verify all aspects
            assertAll("Complete mapping flow",
                // Basic fields
                () -> assertEquals(testOrder.getId(), dto.getId()),
                () -> assertEquals(testOrder.getOrderNumber(), dto.getOrderNumber()),

                // Nested customer
                () -> assertEquals("John Doe", dto.getCustomerName()),
                () -> assertEquals("john.doe@example.com", dto.getCustomerEmail()),

                // Nested address
                () -> assertNotNull(dto.getCustomerAddress()),
                () -> assertEquals("123 Main Street, New York, NY", dto.getCustomerAddress().getFullAddress()),

                // Items
                () -> assertNotNull(dto.getItems()),
                () -> assertEquals(2, dto.getItems().size()),

                // Custom formatting
                () -> assertNotNull(dto.getOrderDateStr()),
                () -> assertEquals("Processing", dto.getStatusDisplay()),
                () -> assertEquals("$2149.93", dto.getTotalAmountDisplay()),

                // Enrichment
                () -> assertNotNull(dto.getChecksum()),
                () -> assertNotNull(dto.getCreatedAtEpoch()),

                // Constant
                () -> assertEquals("MapStruct", dto.getMappedBy())
            );
        }

        @Test
        @org.junit.jupiter.api.Order(21)
        @DisplayName("测试各种订单状态")
        void testAllOrderStatuses() {
            Order.OrderStatus[] statuses = Order.OrderStatus.values();

            for (Order.OrderStatus status : statuses) {
                testOrder.setStatus(status);
                OrderDto dto = orderMapper.toOrderDto(testOrder);

                String expected = status.name().charAt(0) +
                                status.name().substring(1).toLowerCase();
                assertEquals(expected, dto.getStatusDisplay(),
                    "Status " + status + " should display as " + expected);
            }
        }
    }

    /**
     * 创建独立测试订单的辅助方法
     * 避免测试间共享状态导致的状态污染问题
     */
    private Order createTestOrder(Long id, String orderNumber, Order.OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(status);
        order.setCustomer(testCustomer);
        order.setItems(new ArrayList<>(testItems));
        order.setRemarks("Test remarks");
        order.setTotalAmount(100.0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}
