package com.example.demo.sentinel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sentinel 规则展示 VO（用于视图展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelRuleVo {

    /**
     * 规则 ID（用于标识和删除）
     */
    private String ruleId;

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 规则类型名称
     */
    private String ruleTypeName;

    /**
     * 规则类型：FLOW、DEGRADE、PARAM_FLOW、SYSTEM
     */
    private String ruleType;

    /**
     * 阈值显示
     */
    private String thresholdDisplay;

    /**
     * 实际阈值
     */
    private Double threshold;

    /**
     * 限流模式显示
     */
    private String gradeDisplay;

    /**
     * 流控策略显示
     */
    private String controlBehaviorDisplay;

    /**
     * 规则状态
     */
    private String status;

    /**
     * 状态颜色（用于 UI）
     */
    private String statusColor;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 从规则类型创建显示名称
     */
    public static String getRuleTypeDisplayName(String ruleType) {
        switch (ruleType) {
            case "FLOW":
                return "流量控制规则";
            case "DEGRADE":
                return "熔断降级规则";
            case "PARAM_FLOW":
                return "热点参数限流规则";
            case "SYSTEM":
                return "系统保护规则";
            case "AUTHORITY":
                return "授权规则";
            default:
                return "未知规则";
        }
    }

    /**
     * 从限流模式创建显示名称
     */
    public static String getGradeDisplayName(String grade) {
        switch (grade) {
            case "QPS":
                return "QPS 限流";
            case "THREAD":
                return "线程数限流";
            default:
                return grade;
        }
    }

    /**
     * 从控制行为创建显示名称
     */
    public static String getControlBehaviorDisplayName(String behavior) {
        switch (behavior) {
            case "DIRECT":
                return "直接拒绝";
            case "WARM_UP":
                return "Warm Up 预热";
            case "THROTTLING":
                return "匀速排队";
            default:
                return behavior;
        }
    }
}
