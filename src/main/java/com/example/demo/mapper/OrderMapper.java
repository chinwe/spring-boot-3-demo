package com.example.demo.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.TargetType;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderItemDto;
import com.example.demo.entity.Address;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;

/**
 * 订单映射器 - 展示 MapStruct 高级功能
 *
 * 功能特性：
 * 1. 嵌套对象映射
 * 2. 集合映射
 * 3. 自定义映射方法 (@Named)
 * 4. 生命周期回调 (@BeforeMapping, @AfterMapping)
 * 5. 上下文对象传递 (@Context)
 * 6. 默认值和常量
 * 7. 继承配置 (@InheritConfiguration)
 * 8. 表达式映射
 * 9. 空值处理策略
 *
 * @author chinwe
 */
@Mapper(
    componentModel = "spring",
    uses = {AddressMapper.class, OrderItemMapper.class},
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL
)
public abstract class OrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CURRENCY_FORMAT = "$%.2f";

    /**
     * 映射计数器（用于演示生命周期回调）
     */
    private int mappingCounter = 0;

    /**
     * 主要映射方法：Order -> OrderDto
     */
    @Mapping(target = "createdAtEpoch", ignore = true)
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerAddress", source = "customer.address")
    @Mapping(target = "orderDateStr", source = "orderDate", qualifiedByName = "formatDate")
    @Mapping(target = "statusDisplay", source = "status", qualifiedByName = "statusToDisplay")
    @Mapping(target = "totalAmountDisplay", source = "totalAmount", qualifiedByName = "formatOrderCurrency")
    @Mapping(target = "mappedBy", constant = "MapStruct")
    @Mapping(target = "remarks", source = "remarks", defaultValue = "No remarks")
    @Mapping(target = "checksum", ignore = true)
    public abstract OrderDto toOrderDto(Order order);

    /**
     * 反向映射：OrderDto -> Order
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderDate", source = "orderDateStr", qualifiedByName = "parseDate")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Order toOrder(OrderDto orderDto);

    /**
     * 集合映射：List<Order> -> List<OrderDto>
     */
    @InheritConfiguration(name = "toOrderDto")
    public abstract List<OrderDto> toOrderDtoList(List<Order> orders);

    /**
     * 集合映射：List<OrderDto> -> List<Order>
     */
    @InheritConfiguration(name = "toOrder")
    public abstract List<Order> toOrderList(List<OrderDto> orderDtos);

    // ==================== 生命周期回调 ====================

    /**
     * 映射前验证
     */
    @BeforeMapping
    protected void validateOrder(Order order) {
        if (order != null && order.getOrderNumber() == null) {
            throw new IllegalArgumentException("Order number cannot be null");
        }
    }

    /**
     * 映射前日志记录
     */
    @BeforeMapping
    protected void logBeforeMapping(Order order, @TargetType Class<?> targetType) {
        mappingCounter++;
        System.out.println(String.format("[OrderMapper] Mapping #%d: %s -> %s",
            mappingCounter,
            order != null ? order.getClass().getSimpleName() : "null",
            targetType != null ? targetType.getSimpleName() : "null"));
    }

    /**
     * 映射后增强目标对象
     */
    @AfterMapping
    protected void enrichOrderDto(Order order, @MappingTarget OrderDto dto) {
        // 设置时间戳
        if (order != null && order.getCreatedAt() != null) {
            dto.setCreatedAtEpoch(order.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC));
        }

        // 计算校验和
        if (dto != null && dto.getOrderNumber() != null) {
            dto.setChecksum(calculateChecksum(dto.getOrderNumber()));
        }
    }

    // ==================== 自定义映射方法 ====================

    /**
     * 格式化日期
     */
    @Named("formatDate")
    protected String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 解析日期字符串
     */
    @Named("parseDate")
    protected LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 状态枚举转显示文本
     */
    @Named("statusToDisplay")
    protected String statusToDisplay(Order.OrderStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return status.name().charAt(0) + status.name().substring(1).toLowerCase();
    }

    /**
     * 格式化订单货币（避免与 OrderItemMapper.formatCurrency 冲突）
     */
    @Named("formatOrderCurrency")
    protected String formatOrderCurrency(Double amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format(CURRENCY_FORMAT, amount);
    }

    /**
     * 计算校验和（仅内部使用）
     */
    private String calculateChecksum(String input) {
        if (input == null) {
            return null;
        }
        return Integer.toHexString(input.hashCode());
    }

    // ==================== 上下文对象支持 ====================

    /**
     * 使用上下文的映射方法
     */
    public abstract OrderDto toOrderDtoWithContext(Order order, @Context MappingContext context);

    /**
     * 映射上下文类
     */
    public static class MappingContext {
        private final String correlationId;
        private int depth = 0;

        public MappingContext(String correlationId) {
            this.correlationId = correlationId;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public int getDepth() {
            return depth;
        }

        public void incrementDepth() {
            this.depth++;
        }

        @BeforeMapping
        public void beforeMapping() {
            depth++;
        }
    }
}
