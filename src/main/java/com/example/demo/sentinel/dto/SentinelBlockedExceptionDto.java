package com.example.demo.sentinel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Sentinel 阻塞异常信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelBlockedExceptionDto {

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 阻塞类型：FLOW（流控）、DEGRADE（降级）、PARAM_FLOW（热点参数限流）、SYSTEM（系统规则）、AUTHORITY（授权）
     */
    private String blockType;

    /**
     * 阻塞类型描述
     */
    private String blockTypeDescription;

    /**
     * 触发规则
     */
    private String triggeredRule;

    /**
     * 当前阈值
     */
    private double currentThreshold;

    /**
     * 当前实际值
     */
    private double actualValue;

    /**
     * 限制策略（如直接拒绝、Warm Up、匀速排队）
     */
    private String controlBehavior;

    /**
     * 建议等待时间（毫秒）
     */
    private long suggestedWaitTimeMs;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 用户友好的错误消息
     */
    private String userMessage;

    /**
     * 根据异常类型创建 DTO
     */
    public static SentinelBlockedExceptionDto fromBlockException(String resource, String blockType, String rule) {
        String description;
        String userMsg;
        String code;

        switch (blockType) {
            case "FlowException":
                description = "Flow Control (流控规则)";
                userMsg = "请求过于频繁，请稍后再试";
                code = "RATE_LIMIT_EXCEEDED";
                break;
            case "DegradeException":
                description = "Circuit Breaker (熔断降级)";
                userMsg = "服务暂时不可用，请稍后再试";
                code = "SERVICE_DEGRADED";
                break;
            case "ParamFlowException":
                description = "Hotspot Param Flow Control (热点参数限流)";
                userMsg = "热点参数请求过于频繁";
                code = "HOTSPOT_LIMIT_EXCEEDED";
                break;
            case "SystemBlockException":
                description = "System Rule (系统规则)";
                userMsg = "系统负载过高，请稍后再试";
                code = "SYSTEM_LIMIT_EXCEEDED";
                break;
            case "AuthorityException":
                description = "Authority Control (授权控制)";
                userMsg = "无权限访问该资源";
                code = "ACCESS_DENIED";
                break;
            default:
                description = "Unknown Block Type (未知阻塞类型)";
                userMsg = "请求被限制";
                code = "BLOCKED";
                break;
        }

        return SentinelBlockedExceptionDto.builder()
                .resource(resource)
                .blockType(blockType)
                .blockTypeDescription(description)
                .triggeredRule(rule)
                .errorCode(code)
                .userMessage(userMsg)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
