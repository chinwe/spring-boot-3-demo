package com.example.demo.sentinel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Sentinel 统计信息展示 VO（用于视图展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelStatisticsVo {

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 总请求数显示
     */
    private String totalRequestsDisplay;

    /**
     * 成功请求数显示
     */
    private String successRequestsDisplay;

    /**
     * 被拒绝请求数显示
     */
    private String blockedRequestsDisplay;

    /**
     * 当前 QPS 显示
     */
    private String currentQpsDisplay;

    /**
     * 通过率显示
     */
    private String passRateDisplay;

    /**
     * 拒绝率显示
     */
    private String blockRateDisplay;

    /**
     * 平均响应时间显示
     */
    private String avgResponseTimeDisplay;

    /**
     * 异常比例显示
     */
    private String exceptionRateDisplay;

    /**
     * 健康状态描述
     */
    private String healthStatus;

    /**
     * 健康状态颜色（用于 UI）
     */
    private String healthColor;

    /**
     * 建议
     */
    private String recommendation;

    /**
     * 各资源的统计信息
     */
    private Map<String, ResourceStatisticsVo> resourceStatistics;

    /**
     * 系统整体统计
     */
    private SystemStatisticsVo systemStatistics;

    /**
     * 资源统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResourceStatisticsVo {
        private String resource;
        private String passRateDisplay;
        private String blockRateDisplay;
        private String healthStatus;
        private String healthColor;
    }

    /**
     * 系统统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemStatisticsVo {
        private String cpuUsageDisplay;
        private String memoryUsageDisplay;
        private String systemLoadDisplay;
        private String overallHealthStatus;
    }

    /**
     * 根据拒绝率获取健康状态
     */
    public static String getHealthStatus(double blockRate) {
        if (blockRate >= 50) {
            return "严重告警";
        } else if (blockRate >= 20) {
            return "告警";
        } else if (blockRate >= 5) {
            return "注意";
        } else {
            return "健康";
        }
    }

    /**
     * 根据拒绝率获取健康颜色
     */
    public static String getHealthColor(double blockRate) {
        if (blockRate >= 50) {
            return "red";
        } else if (blockRate >= 20) {
            return "orange";
        } else if (blockRate >= 5) {
            return "yellow";
        } else {
            return "green";
        }
    }

    /**
     * 获取建议
     */
    public static String getRecommendation(double blockRate) {
        if (blockRate >= 50) {
            return "系统负载过高，请立即检查或增加阈值";
        } else if (blockRate >= 20) {
            return "系统压力较大，建议增加阈值或进行扩容";
        } else if (blockRate >= 5) {
            return "系统有一定压力，请持续监控";
        } else {
            return "系统运行正常";
        }
    }
}
