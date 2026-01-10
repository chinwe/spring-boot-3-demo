package com.example.demo.service.orika.converter;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

/**
 * Double 货币金额与格式化字符串之间的自定义转换器
 *
 * 功能特性：
 * 1. 将数字金额格式化为货币字符串
 * 2. 处理空值情况
 *
 * @author chinwe
 */
public class CurrencyConverter extends CustomConverter<Double, String> {

    /**
     * 货币格式化模板
     */
    private static final String CURRENCY_FORMAT = "$%.2f";

    /**
     * 将 Double 金额转换为格式化的货币字符串
     *
     * 例如：100.0 -> "$100.00", 99.99 -> "$99.99"
     *
     * @param source 源金额
     * @param destinationType 目标类型
     * @param mappingContext 映射上下文
     * @return 格式化后的货币字符串，如果源对象为 null 则返回 "$0.00"
     */
    @Override
    public String convert(Double source, Type<? extends String> destinationType, MappingContext mappingContext) {
        if (source == null) {
            return "$0.00";
        }
        return String.format(CURRENCY_FORMAT, source);
    }
}
