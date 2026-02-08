package com.example.demo.logging.desensitize.model;

/**
 * 脱敏类型枚举
 * 定义所有支持的敏感信息类型
 */
public enum DesensitizeType {
    /**
     * 邮箱地址
     * 示例: test@example.com -> t***@example.com
     */
    EMAIL("邮箱"),

    /**
     * 手机号码
     * 示例: 13812345678 -> 138****5678
     */
    PHONE("手机号"),

    /**
     * 身份证号
     * 示例: 110101199001011234 -> 110101********1234
     */
    ID_CARD("身份证"),

    /**
     * 银行卡号
     * 示例: 6222021234567890123 -> 6222***********0123
     */
    BANK_CARD("银行卡"),

    /**
     * 密码
     * 示例: password=admin123 -> password=******
     */
    PASSWORD("密码"),

    /**
     * 地址
     * 示例: 北京市朝阳区建国路88号 -> 北京市朝阳区********
     */
    ADDRESS("地址"),

    /**
     * 键值对
     * 示例: password=secret -> password=***
     */
    KEY_VALUE("键值对");

    private final String description;

    DesensitizeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
