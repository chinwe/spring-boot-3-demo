package com.example.demo.logging.desensitize.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 脱敏配置模型
 * 从 YAML 配置文件加载的完整脱敏配置
 */
@Data
public class DesensitizeConfig {
    /**
     * 全局开关
     */
    private boolean enabled = true;

    /**
     * 默认掩码字符
     */
    private char defaultMaskChar = '*';

    /**
     * 脱敏规则列表
     */
    private List<DesensitizeRule> rules = new ArrayList<>();

    /**
     * 键值对配置
     */
    private KeyValueConfig keyValue = new KeyValueConfig();

    /**
     * 性能配置
     */
    private PerformanceConfig performance = new PerformanceConfig();

    /**
     * 键值对配置内部类
     */
    @Data
    public static class KeyValueConfig {
        /**
         * 是否启用键值对脱敏
         */
        private boolean enabled = true;

        /**
         * 键值对分隔符列表
         */
        private List<String> separators = List.of("=", ":", "=>");

        /**
         * 敏感字段名列表
         */
        private List<String> sensitiveKeys = List.of(
            "password", "pwd", "passwd", "token", "apiKey",
            "secret", "accessToken", "refreshToken", "authorization"
        );
    }

    /**
     * 性能配置内部类
     */
    @Data
    public static class PerformanceConfig {
        /**
         * 是否启用正则预编译缓存
         */
        private boolean cachePatterns = true;

        /**
         * 最大缓存数量
         */
        private int maxCacheSize = 100;
    }

    /**
     * 获取启用的规则列表
     * @return 启用的规则列表
     */
    public List<DesensitizeRule> getEnabledRules() {
        return rules.stream()
            .filter(DesensitizeRule::isEnabled)
            .toList();
    }

    /**
     * 根据类型获取规则
     * @param type 脱敏类型
     * @return 对应的规则，如果不存在则返回 null
     */
    public DesensitizeRule getRuleByType(DesensitizeType type) {
        return rules.stream()
            .filter(rule -> rule.getType() == type)
            .findFirst()
            .orElse(null);
    }
}
