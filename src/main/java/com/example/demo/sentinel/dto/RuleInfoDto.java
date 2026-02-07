package com.example.demo.sentinel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sentinel 规则信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleInfoDto {

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 规则类型
     * FLOW - 流量控制规则
     * DEGRADE - 熔断降级规则
     * PARAM_FLOW - 热点参数流控规则
     * SYSTEM - 系统规则
     */
    private String ruleType;

    /**
     * 限流应用（default 或来源应用）
     */
    private String limitApp;

    /**
     * 阈值类型
     * THREAD_COUNT - 线程数
     * QPS - 每秒请求数
     */
    private String grade;

    /**
     * 阈值
     */
    private String count;

    /**
     * 流控策略（0-直接拒绝，1-关联，2-链路）
     */
    private String strategy;

    /**
     * 流控效果（0-快速失败，1-Warm Up，2-排队等待）
     */
    private String controlBehavior;

    /**
     * 熔断策略（0-慢调用比例，1-异常比例，2-异常数）
     */
    private String degradeGrade;

    /**
     * 熔断降级规则的相关字段
     */
    private String timeWindow;
    private String minRequestAmount;
    private String statIntervalMs;

    /**
     * 热点参数索引
     */
    private String paramIdx;

    /**
     * 统计时长（秒）
     */
    private String durationInSec;

    /**
     * 系统规则相关字段
     */
    private String highestSystemLoad;
    private String highestCpuUsage;
    private String maxRt;
    private String concurrency;
    private String qps;
}
