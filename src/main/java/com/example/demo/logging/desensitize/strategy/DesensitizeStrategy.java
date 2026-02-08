package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;

/**
 * 脱敏策略接口
 * 定义敏感信息脱敏的统一接口
 */
public interface DesensitizeStrategy {

    /**
     * 对输入的敏感信息进行脱敏处理
     *
     * @param input 原始输入
     * @param rule  脱敏规则
     * @return 脱敏后的字符串
     */
    String desensitize(String input, DesensitizeRule rule);

    /**
     * 检查输入是否匹配脱敏规则
     *
     * @param input 原始输入
     * @param rule  脱敏规则
     * @return 是否匹配
     */
    boolean matches(String input, DesensitizeRule rule);

    /**
     * 生成掩码字符串
     *
     * @param length 掩码长度
     * @param rule   脱敏规则
     * @return 掩码字符串
     */
    default String generateMask(int length, DesensitizeRule rule) {
        return String.valueOf(rule.getMaskChar()).repeat(Math.max(1, length));
    }
}
