package com.example.demo.logging.desensitize.model;

import lombok.Data;

import java.util.List;

/**
 * 脱敏规则模型
 * 定义单个敏感信息类型的脱敏规则
 */
@Data
public class DesensitizeRule {
    /**
     * 脱敏类型
     */
    private DesensitizeType type;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 正则表达式模式（用于匹配敏感信息）
     */
    private String pattern;

    /**
     * 保留前缀长度
     */
    private int keepPrefix = 0;

    /**
     * 保留后缀长度
     */
    private int keepSuffix = 0;

    /**
     * 掩码字符
     */
    private char maskChar = '*';

    /**
     * 规则描述
     */
    private String description;

    /**
     * 键值对字段名列表（仅用于 KEY_VALUE 类型）
     */
    private List<String> keyNames;

    /**
     * 获取掩码字符串
     * @return 掩码字符串
     */
    public String getMaskString() {
        return String.valueOf(maskChar);
    }

    /**
     * 检查是否需要保留前缀
     * @return 是否保留前缀
     */
    public boolean hasKeepPrefix() {
        return keepPrefix > 0;
    }

    /**
     * 检查是否需要保留后缀
     * @return 是否保留后缀
     */
    public boolean hasKeepSuffix() {
        return keepSuffix > 0;
    }
}
