package com.example.demo.logging.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 脱敏测试响应 DTO
 */
@Data
@Builder
public class DesensitizeTestResponse {
    /**
     * 脱敏类型
     */
    private String type;

    /**
     * 原始值
     */
    private String original;

    /**
     * 脱敏后的值（仅用于演示，实际日志中不返回）
     */
    private String desensitized;

    /**
     * 响应消息
     */
    private String message;
}
