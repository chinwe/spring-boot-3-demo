package com.example.demo.controller.orika;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.OrderDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Order.OrderStatus;
import com.example.demo.service.orika.OrikaMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Orika 对象映射控制器
 *
 * 功能特性：
 * 1. 演示 Orika 基础对象映射
 * 2. 演示复杂嵌套对象映射
 * 3. 演示集合映射
 * 4. 演示批量映射性能
 *
 * @author chinwe
 */
@Slf4j
@RestController
@RequestMapping("/orika")
@Tag(name = "Orika Mapping", description = "Orika 对象映射演示接口")
public class OrikaController {

    @Resource
    private OrikaMappingService mappingService;

    /**
     * 基础对象映射演示
     *
     * GET /orika/user
     *
     * 展示 Orika 的基础映射功能：
     * - 同名字段自动映射
     * - 类型安全保证
     *
     * @return 映射后的 UserDto 对象
     */
    @GetMapping("/user")
    @Operation(summary = "基础对象映射", description = "演示简单的 User 对象属性映射")
    public UserDto mapUser() {
        log.info("Received request for basic user mapping");

        // 创建测试 User 对象
        User user = createSampleUser();

        // 使用 Orika 进行映射
        UserDto dto = mappingService.toUserDto(user);

        log.info("Successfully mapped User to UserDto");
        return dto;
    }

    /**
     * 复杂对象映射演示
     *
     * GET /orika/order
     *
     * 展示 Orika 的高级映射功能：
     * - 嵌套对象映射（customer.fullName -> customerName）
     * - 自定义转换器（日期、枚举）
     * - 字段重命名
     * - 计算字段
     *
     * @return 映射后的 OrderDto 对象
     */
    @GetMapping("/order")
    @Operation(summary = "复杂对象映射", description = "演示嵌套对象、自定义转换器等高级功能")
    public OrderDto mapOrder() {
        log.info("Received request for complex order mapping");

        // 创建测试 Order 对象
        Order order = createSampleOrder();

        // 使用 Orika 进行映射
        OrderDto dto = mappingService.toOrderDto(order);

        log.info("Successfully mapped Order to OrderDto: orderNumber={}, mappedBy={}",
            dto.getOrderNumber(), dto.getMappedBy());

        return dto;
    }

    /**
     * 集合映射演示
     *
     * GET /orika/orders?count=3
     *
     * 展示 Orika 的集合映射功能
     *
     * @param count 要生成的订单数量，默认为 3
     * @return 映射后的 OrderDto 列表
     */
    @GetMapping("/orders")
    @Operation(summary = "集合映射", description = "演示列表对象映射")
    public List<OrderDto> mapOrders(
            @Parameter(description = "订单数量")
            @RequestParam(defaultValue = "3") int count) {
        log.info("Received request for collection mapping with count: {}", count);

        // 创建测试 Order 列表
        List<Order> orders = createSampleOrders(count);

        // 使用 Orika 进行集合映射
        List<OrderDto> dtoList = mappingService.toOrderDtoList(orders);

        log.info("Successfully mapped {} Orders to OrderDto list", dtoList.size());

        return dtoList;
    }

    /**
     * 批量映射演示
     *
     * POST /orika/batch
     *
     * 展示 Orika 的批量映射和性能优化功能
     *
     * @param orders 要映射的 Order 列表
     * @return 映射后的 OrderDto 列表
     */
    @PostMapping("/batch")
    @Operation(summary = "批量映射", description = "演示批量操作和性能优化（并行流处理）")
    public List<OrderDto> batchMapOrders(@RequestBody List<Order> orders) {
        log.info("Received request for batch mapping with {} orders", orders.size());

        long startTime = System.currentTimeMillis();

        // 使用 Orika 进行批量映射（并行流处理）
        List<OrderDto> dtoList = mappingService.toOrderDtoListBatch(orders);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully batch mapped {} Orders in {} ms ({} ms/order)",
            dtoList.size(), duration, String.format("%.2f", (double) duration / orders.size()));

        return dtoList;
    }

    // ==================== 测试数据创建方法 ====================

    /**
     * 创建测试 User 对象
     */
    private User createSampleUser() {
        return new User(1L, "张三", "zhangsan@example.com");
    }

    /**
     * 创建测试 Order 对象
     */
    private Order createSampleOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-2025-001");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PROCESSING);
        order.setRemarks("加急处理");
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

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductName("无线鼠标");
        item2.setQuantity(2);
        item2.setUnitPrice(99.99);

        order.addItem(item1);
        order.addItem(item2);

        return order;
    }

    /**
     * 创建测试 Order 列表
     */
    private List<Order> createSampleOrders(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setOrderNumber("ORD-2025-" + String.format("%03d", i + 1));
            order.setOrderDate(LocalDateTime.now().minusDays(i));
            order.setStatus(OrderStatus.values()[i % OrderStatus.values().length]);
            order.setRemarks("测试订单 " + (i + 1));
            order.setTotalAmount(100.0 + i * 50);
            order.setCreatedAt(LocalDateTime.now().minusDays(i));
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
