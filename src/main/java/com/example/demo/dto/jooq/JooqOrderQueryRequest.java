package com.example.demo.dto.jooq;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Size(max = 50, message = "订单号长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "订单号格式不正确")
    private String orderNumber;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
