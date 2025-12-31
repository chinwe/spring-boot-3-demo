package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项数据传输对象
 *
 * @author chinwe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private String productCode;

    private String productName;

    private Integer quantity;

    private String unitPriceDisplay;

    private String subtotalDisplay;
}
