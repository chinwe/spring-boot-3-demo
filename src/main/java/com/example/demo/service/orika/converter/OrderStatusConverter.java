package com.example.demo.service.orika.converter;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import com.example.demo.entity.Order;

/**
 * OrderStatus 枚举与 String 之间的自定义转换器
 *
 * 功能特性：
 * 1. 将枚举转换为友好的显示文本
 * 2. 将字符串转换回枚举
 * 3. 处理空值情况
 *
 * @author chinwe
 */
public class OrderStatusConverter extends CustomConverter<Order.OrderStatus, String> {

    /**
     * 将 OrderStatus 枚举转换为友好的显示文本
     *
     * 例如：PENDING -> Pending, PROCESSING -> Processing
     *
     * @param source 源枚举值
     * @param destinationType 目标类型
     * @param mappingContext 映射上下文
     * @return 格式化后的字符串，如果源对象为 null 则返回 "UNKNOWN"
     */
    @Override
    public String convert(Order.OrderStatus source, Type<? extends String> destinationType, MappingContext mappingContext) {
        if (source == null) {
            return "UNKNOWN";
        }
        // 将枚举转换为首字母大写、其余小写的格式
        // 例如：PENDING -> Pending
        return source.name().charAt(0) + source.name().substring(1).toLowerCase();
    }
}
