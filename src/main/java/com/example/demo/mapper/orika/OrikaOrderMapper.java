package com.example.demo.mapper.orika;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Order;

/**
 * Orika Order 映射器 - MapStruct 实现
 *
 * 功能特性：
 * 1. Order -> OrderDto 映射
 * 2. 嵌套对象映射（customer.fullName -> customerName）
 * 3. 自定义转换器（LocalDateTime -> String, Enum -> String）
 * 4. 字段重命名
 *
 * @author chinwe
 */
@Mapper(
    componentModel = "spring",
    uses = {OrikaAddressMapper.class, OrikaOrderItemMapper.class},
    nullValueMappingStrategy = org.mapstruct.NullValueMappingStrategy.RETURN_NULL
)
public interface OrikaOrderMapper {

    /**
     * 日期时间格式化器
     */
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Order -> OrderDto 映射
     *
     * 展示高级映射功能：
     * - 嵌套对象映射（customer.fullName -> customerName）
     * - 自定义转换器（LocalDateTime -> String, Enum -> String）
     * - 字段重命名
     */
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerAddress", source = "customer.address")
    @Mapping(target = "orderDateStr", source = "orderDate", qualifiedByName = "formatDate")
    @Mapping(target = "statusDisplay", source = "status", qualifiedByName = "statusToDisplay")
    @Mapping(target = "totalAmountDisplay", source = "totalAmount", qualifiedByName = "formatOrderCurrency")
    @Mapping(target = "createdAtEpoch", ignore = true)
    @Mapping(target = "mappedBy", ignore = true)
    @Mapping(target = "checksum", ignore = true)
    OrderDto toOrderDto(Order order);

    /**
     * 集合映射：List<Order> -> List<OrderDto>
     */
    List<OrderDto> toOrderDtoList(List<Order> orders);

    /**
     * 格式化日期
     */
    @Named("formatDate")
    default String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 状态枚举转显示文本
     * 与 Orika OrderStatusConverter 相同的逻辑
     */
    @Named("statusToDisplay")
    default String statusToDisplay(Order.OrderStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        // 将枚举转换为首字母大写、其余小写的格式
        // 例如：PENDING -> Pending
        return status.name().charAt(0) + status.name().substring(1).toLowerCase();
    }

    /**
     * 格式化订单货币
     * 与 Orika CurrencyConverter 相同的逻辑
     */
    @Named("formatOrderCurrency")
    default String formatOrderCurrency(Double amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", amount);
    }
}
