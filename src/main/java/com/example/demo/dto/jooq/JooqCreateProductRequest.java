package com.example.demo.dto.jooq;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建商品请求
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqCreateProductRequest {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Digits(integer = 10, fraction = 2, message = "价格最多2位小数")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 1000000, message = "库存超出合理范围")
    private Integer stock;

    private String category;
}
