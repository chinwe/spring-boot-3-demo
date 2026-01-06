package com.example.demo.dto.jooq;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询请求
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqOrderQueryRequest {

    private Long userId;

    private String status;

    private String orderNumber;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
