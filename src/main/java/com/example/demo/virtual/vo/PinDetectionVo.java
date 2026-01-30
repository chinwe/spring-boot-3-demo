package com.example.demo.virtual.vo;

import com.example.demo.virtual.dto.PinDetectionReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pin 检测视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinDetectionVo {

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
    private List<PinDetectionReport.PinEvent> pinEvents;

    /**
     * 总 Pin 事件数
     */
    private Integer totalPinEvents;
}
