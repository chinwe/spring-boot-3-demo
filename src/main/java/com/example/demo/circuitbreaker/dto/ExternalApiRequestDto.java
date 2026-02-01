package com.example.demo.circuitbreaker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外部API请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "外部API请求")
public class ExternalApiRequestDto {

    @NotBlank(message = "API端点不能为空")
    @Schema(description = "API端点路径", example = "/api/users")
    private String endpoint;

    @Schema(description = "是否模拟失败", example = "false")
    private boolean simulateFailure;

    @Schema(description = "是否模拟延迟（毫秒）", example = "1000")
    private Long delayMs;

    @Schema(description = "请求负载", example = "{\"key\": \"value\"}")
    private String payload;
}
