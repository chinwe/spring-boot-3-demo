package com.example.demo.dto.jooq;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 订单项 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqOrderItemDto {

    private Long id;

    private Long orderId;

    private Long productId;

    private String productName; // 关联查询时填充

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}
