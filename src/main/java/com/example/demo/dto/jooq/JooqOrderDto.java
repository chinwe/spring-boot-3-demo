package com.example.demo.dto.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 订单 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqOrderDto {

    private Long id;

    private String orderNumber;

    private Long userId;

    private String username; // 关联查询时填充

    private BigDecimal totalAmount;

    private String status;

    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<JooqOrderItemDto> items;
}
