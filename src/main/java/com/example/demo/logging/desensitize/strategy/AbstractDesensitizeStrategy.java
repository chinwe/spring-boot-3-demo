package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 脱敏策略抽象基类
 * 提供通用的正则匹配和脱敏处理逻辑
 * 支持正则表达式缓存以提升性能
 */
public abstract class AbstractDesensitizeStrategy implements DesensitizeStrategy {

    /**
     * 正则表达式缓存
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    /**
     * 最大缓存数量
     */
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * 是否启用缓存（从配置读取）
     */
    private static volatile boolean cacheEnabled = true;

    /**
     * 设置缓存启用状态
     *
     * @param config 脱敏配置
     */
    public static void setCacheEnabled(DesensitizeConfig config) {
        if (config != null && config.getPerformance() != null) {
            cacheEnabled = config.getPerformance().isCachePatterns();
        }
    }

    /**
     * 获取支持的脱敏类型
     *
     * @return 脱敏类型
     */
    public abstract DesensitizeType getSupportedType();

    /**
     * 编译正则表达式（带缓存）
     *
     * @param regex 正则表达式字符串
     * @return 编译后的 Pattern
     * @throws IllegalArgumentException 如果正则表达式语法错误
     */
    protected Pattern compilePattern(String regex) {
        if (regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("Regex pattern cannot be null or empty");
        }

        // 如果启用缓存，先从缓存获取
        if (cacheEnabled) {
            Pattern cached = PATTERN_CACHE.get(regex);
            if (cached != null) {
                return cached;
            }
        }

        // 缓存未命中，编译正则表达式
        try {
            Pattern pattern = Pattern.compile(regex);

            // 如果启用缓存且缓存未满，加入缓存
            if (cacheEnabled && PATTERN_CACHE.size() < MAX_CACHE_SIZE) {
                PATTERN_CACHE.put(regex, pattern);
            }

            return pattern;
        } catch (PatternSyntaxException e) {
            // 抛出更清晰的异常信息
            throw new IllegalArgumentException("Invalid regex pattern: " + regex, e);
        }
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        PATTERN_CACHE.clear();
    }

    /**
     * 获取缓存大小
     *
     * @return 当前缓存的 Pattern 数量
     */
    public static int getCacheSize() {
        return PATTERN_CACHE.size();
    }

    /**
     * 检查规则是否支持
     *
     * @param rule 脱敏规则
     * @return 是否支持
     */
    protected boolean isRuleSupported(DesensitizeRule rule) {
        return rule != null && rule.getType() == getSupportedType();
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null || rule.getPattern() == null) {
            return false;
        }
        try {
            return compilePattern(rule.getPattern()).matcher(input).find();
        } catch (IllegalArgumentException e) {
            // 正则表达式错误，记录错误并返回 false
            System.err.println("[DESENSITIZE ERROR] Failed to compile pattern for rule " +
                rule.getType() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * 执行脱敏处理
     *
     * @param input   原始输入
     * @param rule    脱敏规则
     * @param pattern 编译后的正则表达式
     * @return 脱敏后的字符串
     */
    protected String doDesensitize(String input, DesensitizeRule rule, Pattern pattern) {
        var matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        matcher.reset();
        var sb = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group();
            String desensitized = desensitizeMatched(matched, rule);
            matcher.appendReplacement(sb, desensitized);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 对匹配到的字符串进行脱敏
     *
     * @param matched 匹配到的字符串
     * @param rule    脱敏规则
     * @return 脱敏后的字符串
     */
    protected String desensitizeMatched(String matched, DesensitizeRule rule) {
        if (rule == null) {
            return matched;
        }

        int length = matched.length();
        int keepPrefix = Math.min(rule.getKeepPrefix(), length);
        int keepSuffix = Math.min(rule.getKeepSuffix(), length - keepPrefix);

        if (keepPrefix + keepSuffix >= length) {
            return matched;
        }

        String prefix = matched.substring(0, keepPrefix);
        String suffix = matched.substring(length - keepSuffix);
        int maskLength = length - keepPrefix - keepSuffix;

        return prefix + generateMask(maskLength, rule) + suffix;
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }
        String pattern = rule.getPattern();
        if (pattern == null || pattern.isEmpty()) {
            return input;
        }

        try {
            return doDesensitize(input, rule, compilePattern(pattern));
        } catch (IllegalArgumentException e) {
            // 正则表达式错误，记录错误并返回原始输入
            System.err.println("[DESENSITIZE ERROR] Failed to desensitize with rule " +
                rule.getType() + ": " + e.getMessage());
            return input;
        }
    }
}
