package com.example.demo.sentinel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sentinel 流控结果展示 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelFlowControlVo {

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 是否通过（未被限流）
     */
    private boolean passed;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 限流规则描述
     */
    private String ruleDescription;

    /**
     * 当前 QPS
     */
    private double currentQps;

    /**
     * 限流阈值
     */
    private double threshold;

    /**
     * 限流模式
     */
    private String grade;

    /**
     * 控制行为
     */
    private String controlBehavior;

    /**
     * 等待时间（毫秒，用于排队模式）
     */
    private long waitTimeMs;

    /**
     * 结果类型：SUCCESS、BLOCKED、QUEUED
     */
    private String resultType;

    /**
     * 结果类型显示
     */
    private String resultTypeDisplay;

    /**
     * 状态颜色（用于 UI）
     */
    private String statusColor;

    /**
     * 创建成功结果
     */
    public static SentinelFlowControlVo success(String resource, double currentQps, double threshold) {
        return SentinelFlowControlVo.builder()
                .resource(resource)
                .passed(true)
                .message("请求通过")
                .ruleDescription("当前 QPS: " + String.format("%.2f", currentQps) + ", 阈值: " + threshold)
                .currentQps(currentQps)
                .threshold(threshold)
                .resultType("SUCCESS")
                .resultTypeDisplay("通过")
                .statusColor("green")
                .build();
    }

    /**
     * 创建被限流结果
     */
    public static SentinelFlowControlVo blocked(String resource, String grade, String controlBehavior, double threshold) {
        return SentinelFlowControlVo.builder()
                .resource(resource)
                .passed(false)
                .message("请求被限流拦截")
                .ruleDescription("限流模式: " + grade + ", 控制行为: " + controlBehavior + ", 阈值: " + threshold)
                .grade(grade)
                .controlBehavior(controlBehavior)
                .threshold(threshold)
                .resultType("BLOCKED")
                .resultTypeDisplay("被拦截")
                .statusColor("red")
                .build();
    }

    /**
     * 创建排队等待结果
     */
    public static SentinelFlowControlVo queued(String resource, long waitTimeMs) {
        return SentinelFlowControlVo.builder()
                .resource(resource)
                .passed(true)
                .message("请求进入排队等待")
                .ruleDescription("预计等待时间: " + waitTimeMs + " ms")
                .waitTimeMs(waitTimeMs)
                .resultType("QUEUED")
                .resultTypeDisplay("排队中")
                .statusColor("yellow")
                .build();
    }
}
