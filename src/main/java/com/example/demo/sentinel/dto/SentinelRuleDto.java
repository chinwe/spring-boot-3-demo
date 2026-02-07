package com.example.demo.sentinel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sentinel 规则定义 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelRuleDto {

    /**
     * 规则类型：FLOW（流控）、DEGRADE（降级）、PARAM_FLOW（热点参数）、SYSTEM（系统规则）
     */
    private String ruleType;

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 限流阈值（QPS 或 线程数）
     */
    private Double count;

    /**
     * 限流模式：QPS、THREAD
     */
    private String grade;

    /**
     * 流控策略：DIRECT（直接）、RELATE（关联）、LINK（链路）
     */
    private String controlBehavior;

    /**
     * 降级策略：SLOW_REQUEST_RATIO（慢调用比例）、EXCEPTION_RATIO（异常比例）、EXCEPTION_COUNT（异常数）
     */
    private String degradeStrategy;

    /**
     * 慢调用比例阈值（仅用于降级规则）
     */
    private Double slowRatioThreshold;

    /**
     * 最小请求数（仅用于降级规则）
     */
    private Integer minRequestAmount;

    /**
     * 熔断时长（秒）
     */
    private Integer timeWindow;

    /**
     * 统计时长（秒）
     */
    private Integer statisticIntervalSec;

    /**
     * 热点参数索引（从0开始）
     */
    private Integer paramIdx;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
