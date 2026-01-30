package com.example.demo.virtual.dto;

import com.example.demo.virtual.vo.PinDetectionVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pin 检测报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinDetectionReport {

    /**
     * 报告 ID
     */
    private String reportId;

    /**
     * 检测开始时间
     */
    private LocalDateTime startTime;

    /**
     * 检测结束时间
     */
    private LocalDateTime endTime;

    /**
     * 检测时长（毫秒）
     */
    private Long durationMillis;

    /**
     * 检测到的 Pin 事件列表
     */
    @Builder.Default
    private List<PinEvent> pinEvents = new ArrayList<>();

    /**
     * 总 Pin 事件数
     */
    private Integer totalPinEvents;

    /**
     * Pin 事件详细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PinEvent {
        /**
         * 事件时间
         */
        private LocalDateTime eventTime;

        /**
         * Pin 位置
         */
        private String pinLocation;

        /**
         * 持续时长（毫秒）
         */
        private Long durationMillis;

        /**
         * 线程名称
         */
        private String threadName;

        /**
         * Pin 类型
         */
        private PinType pinType;

        /**
         * 描述
         */
        private String description;
    }

    /**
     * Pin 类型
     */
    public enum PinType {
        /**
         * synchronized 块
         */
        SYNCHRONIZED_BLOCK,
        /**
         * 本地方法调用
         */
        NATIVE_METHOD,
        /**
         * 文件 I/O
         */
        FILE_IO,
        /**
         * 套接字 I/O
         */
        SOCKET_IO,
        /**
         * 其他
         */
        OTHER
    }

    /**
     * 转换为 VO
     */
    public PinDetectionVo toVo() {
        return PinDetectionVo.builder()
                .reportId(reportId)
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(durationMillis)
                .pinEvents(pinEvents)
                .totalPinEvents(totalPinEvents)
                .build();
    }
}
