package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项实体类
 *
 * @author chinwe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Long id;

    private String productCode;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    /**
     * 计算小计
     */
    public Double getSubtotal() {
        return quantity * unitPrice;
    }
}
