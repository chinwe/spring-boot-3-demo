package com.example.demo.circuitbreaker.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 熔断器指标 VO（用于视图展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsVo {

    /**
     * 熔断器名称
     */
    private String name;

    /**
     * 健康评分（0-100）
     */
    private Integer healthScore;

    /**
     * 总调用次数（格式化）
     */
    private String totalCallsDisplay;

    /**
     * 成功率百分比
     */
    private String successRate;

    /**
     * 状态指示器
     */
    private String statusIndicator;

    /**
     * 限流器指标摘要
     */
    private String rateLimiterSummary;

    /**
     * 性能等级
     */
    private String performanceGrade;

    /**
     * 其他指标
     */
    private Map<String, String> additionalMetrics;

    /**
     * 计算健康评分
     */
    public static int calculateHealthScore(long totalCalls, long successfulCalls, long failedCalls) {
        if (totalCalls == 0) {
            return 100;
        }
        double successRate = (double) successfulCalls / totalCalls;
        return (int) (successRate * 100);
    }

    /**
     * 获取性能等级
     */
    public static String getPerformanceGrade(int healthScore) {
        if (healthScore >= 90) {
            return "A (优秀)";
        } else if (healthScore >= 75) {
            return "B (良好)";
        } else if (healthScore >= 60) {
            return "C (一般)";
        } else if (healthScore >= 40) {
            return "D (较差)";
        } else {
            return "F (差)";
        }
    }
}
