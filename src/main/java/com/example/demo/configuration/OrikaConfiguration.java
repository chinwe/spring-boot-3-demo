package com.example.demo.configuration;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entity.Order;
import com.example.demo.dto.OrderDto;
import com.example.demo.service.orika.converter.LocalDateTimeConverter;
import com.example.demo.service.orika.converter.OrderStatusConverter;
import com.example.demo.service.orika.converter.CurrencyConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * Orika 配置类
 *
 * 功能特性：
 * 1. 配置 MapperFactory Bean
 * 2. 注册自定义转换器
 * 3. 配置字段映射规则
 * 4. 提供 MapperFacade Bean
 *
 * @author chinwe
 */
@Slf4j
@Configuration
public class OrikaConfiguration {

    /**
     * 配置 Orika MapperFactory
     *
     * MapperFactory 是 Orika 的核心配置类，负责：
     * - 注册自定义转换器
     * - 配置字段映射规则
     * - 构建映射器实例
     */
    @Bean
    public MapperFactory mapperFactory() {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder().build();

        // 注册自定义转换器
        registerConverters(factory);

        // 配置 Order -> OrderDto 映射规则
        configureOrderMapping(factory);

        log.info("Orika MapperFactory initialized successfully");

        return factory;
    }

    /**
     * 配置 MapperFacade Bean
     *
     * MapperFacade 是线程安全的，可以并发使用
     * 它是执行对象映射的主要入口
     */
    @Bean
    public MapperFacade mapperFacade(MapperFactory mapperFactory) {
        MapperFacade facade = mapperFactory.getMapperFacade();
        log.info("Orika MapperFacade initialized successfully");
        return facade;
    }

    /**
     * 注册自定义转换器
     *
     * Orika 使用 CustomConverter 来处理复杂类型的转换
     */
    private void registerConverters(MapperFactory factory) {
        // 日期时间转换器
        factory.getConverterFactory().registerConverter(new LocalDateTimeConverter());

        // 订单状态枚举转换器
        factory.getConverterFactory().registerConverter(new OrderStatusConverter());

        // 货币转换器（使用命名转换器）
        factory.getConverterFactory().registerConverter("currencyConverter", new CurrencyConverter());

        log.info("Orika custom converters registered successfully");
    }

    /**
     * 配置 Order -> OrderDto 的映射规则
     *
     * 展示 Orika 的字段映射功能：
     * - 嵌套对象映射
     * - 字段重命名
     * - 自定义转换器应用
     */
    private void configureOrderMapping(MapperFactory factory) {
        factory.classMap(Order.class, OrderDto.class)
            // 嵌套对象映射 - 将 customer.fullName 映射到 customerName
            .field("customer.fullName", "customerName")

            // 嵌套对象映射 - 将 customer.email 映射到 customerEmail
            .field("customer.email", "customerEmail")

            // 嵌套对象映射 - 将 customer.address 映射到 customerAddress
            .field("customer.address", "customerAddress")

            // 日期转换 - 使用自定义转换器
            .field("orderDate", "orderDateStr")

            // 枚举转换 - 使用自定义转换器
            .field("status", "statusDisplay")

            // 货币格式化 - 使用命名的自定义转换器
            .fieldMap("totalAmount", "totalAmountDisplay").converter("currencyConverter").add()

            // 其他字段按名称自动映射
            .byDefault()

            // 注册映射配置
            .register();

        log.info("Orika Order -> OrderDto mapping configured successfully");
    }
}
