package com.example.demo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.demo.dto.OrderItemDto;
import com.example.demo.entity.OrderItem;

/**
 * 订单项映射器
 *
 * @author chinwe
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    /**
     * OrderItem -> OrderItemDto
     */
    @Mapping(target = "unitPriceDisplay", source = "unitPrice", qualifiedByName = "formatCurrency")
    @Mapping(target = "subtotalDisplay", source = ".", qualifiedByName = "calculateSubtotal")
    OrderItemDto toOrderItemDto(OrderItem item);

    /**
     * OrderItemDto -> OrderItem
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unitPrice", source = "unitPriceDisplay", qualifiedByName = "parseCurrency")
    OrderItem toOrderItem(OrderItemDto dto);

    /**
     * 集合映射
     */
    List<OrderItemDto> toOrderItemDtoList(List<OrderItem> items);

    /**
     * 格式化货币
     */
    @Named("formatCurrency")
    default String formatCurrency(Double amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", amount);
    }

    /**
     * 解析货币字符串
     */
    @Named("parseCurrency")
    default Double parseCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(currencyStr.replace("$", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 计算并格式化小计
     */
    @Named("calculateSubtotal")
    default String calculateSubtotal(OrderItem item) {
        if (item == null || item.getQuantity() == null || item.getUnitPrice() == null) {
            return "$0.00";
        }
        double subtotal = item.getQuantity() * item.getUnitPrice();
        return String.format("$%.2f", subtotal);
    }
}
