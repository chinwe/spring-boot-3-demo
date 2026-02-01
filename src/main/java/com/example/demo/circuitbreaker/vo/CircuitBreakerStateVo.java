package com.example.demo.circuitbreaker.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 熔断器状态 VO（用于视图展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerStateVo {

    /**
     * 熔断器名称
     */
    private String name;

    /**
     * 当前状态中文描述
     */
    private String stateDescription;

    /**
     * 当前状态：CLOSED, OPEN, HALF_OPEN
     */
    private String state;

    /**
     * 状态颜色（用于UI展示）
     */
    private String stateColor;

    /**
     * 失败率百分比
     */
    private String failureRateDisplay;

    /**
     * 健康状态描述
     */
    private String healthStatus;

    /**
     * 建议
     */
    private String recommendation;

    /**
     * 从状态值获取状态描述
     */
    public static CircuitBreakerStateVo fromState(String name, String state, double failureRate) {
        String description;
        String color;
        String health;
        String recommendation;

        switch (state) {
            case "OPEN":
                description = "熔断器已打开";
                color = "red";
                health = "不健康";
                recommendation = "等待熔断器自动恢复到半开状态";
                break;
            case "HALF_OPEN":
                description = "熔断器半开（试探中）";
                color = "yellow";
                health = "恢复中";
                recommendation = "熔断器正在试探性接受请求";
                break;
            case "CLOSED":
            default:
                description = "熔断器关闭（正常）";
                color = "green";
                health = "健康";
                recommendation = "系统运行正常";
                break;
        }

        return CircuitBreakerStateVo.builder()
                .name(name)
                .state(state)
                .stateDescription(description)
                .stateColor(color)
                .failureRateDisplay(String.format("%.2f%%", failureRate))
                .healthStatus(health)
                .recommendation(recommendation)
                .build();
    }
}
