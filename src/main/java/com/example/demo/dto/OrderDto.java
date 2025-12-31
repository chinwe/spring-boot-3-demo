package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单数据传输对象
 *
 * @author chinwe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;

    private String orderNumber;

    private String orderDateStr;

    private String statusDisplay;

    private String customerName;

    private String customerEmail;

    private AddressDto customerAddress;

    private List<OrderItemDto> items;

    private String remarks;

    private String totalAmountDisplay;

    private Long createdAtEpoch;

    private String mappedBy;

    private String checksum;
}
