package com.example.demo.virtual.dto;

import com.example.demo.virtual.vo.VirtualThreadTaskVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 虚拟线程任务数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualThreadTaskDto {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 执行时长（毫秒）
     */
    private Long durationMillis;

    /**
     * 执行的线程名称
     */
    private String threadName;

    /**
     * 是否为虚拟线程
     */
    private Boolean isVirtualThread;

    /**
     * 任务结果
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    /**
     * 转换为 VO
     */
    public VirtualThreadTaskVo toVo() {
        return VirtualThreadTaskVo.builder()
                .taskId(taskId)
                .taskName(taskName)
                .status(status)
                .createdAt(createdAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .durationMillis(durationMillis)
                .threadName(threadName)
                .isVirtualThread(isVirtualThread)
                .result(result)
                .errorMessage(errorMessage)
                .build();
    }
}
