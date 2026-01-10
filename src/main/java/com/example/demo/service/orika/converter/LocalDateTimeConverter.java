package com.example.demo.service.orika.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

/**
 * LocalDateTime 与 String 之间的自定义转换器
 *
 * 功能特性：
 * 1. 将 LocalDateTime 格式化为字符串
 * 2. 将字符串解析为 LocalDateTime
 * 3. 处理空值情况
 *
 * @author chinwe
 */
public class LocalDateTimeConverter extends CustomConverter<LocalDateTime, String> {

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * LocalDateTime 转 String
     *
     * @param source 源对象
     * @param destinationType 目标类型
     * @param mappingContext 映射上下文
     * @return 格式化后的字符串，如果源对象为 null 则返回 null
     */
    @Override
    public String convert(LocalDateTime source, Type<? extends String> destinationType, MappingContext mappingContext) {
        if (source == null) {
            return null;
        }
        return source.format(DATE_FORMATTER);
    }
}
