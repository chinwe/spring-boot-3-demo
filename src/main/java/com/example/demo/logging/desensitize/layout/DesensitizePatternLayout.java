package com.example.demo.logging.desensitize.layout;

import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import com.example.demo.logging.desensitize.strategy.DesensitizeStrategy;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志脱敏 Layout
 * 包装 PatternLayout，在格式化日志时进行脱敏处理
 */
@Plugin(name = "DesensitizePatternLayout", category = "Core", elementType = "layout", printObject = true)
public class DesensitizePatternLayout extends AbstractStringLayout {

    /**
     * 静态配置持有者（由 Spring 初始化后设置）
     */
    private static volatile DesensitizeConfig staticConfig;
    private static volatile Map<DesensitizeType, DesensitizeStrategy> staticStrategyMap = new ConcurrentHashMap<>();

    /**
     * 错误计数器（用于监控）
     */
    private static final AtomicLong errorCount = new AtomicLong(0);

    /**
     * 委托的 PatternLayout
     */
    private final PatternLayout delegateLayout;

    /**
     * 静态设置方法：由 Spring 配置类调用，设置脱敏配置
     */
    public static void setStaticConfig(DesensitizeConfig config) {
        staticConfig = config;
    }

    /**
     * 静态设置方法：由 Spring 配置类调用，设置策略映射
     */
    public static void setStaticStrategyMap(Map<DesensitizeType, DesensitizeStrategy> strategyMap) {
        if (strategyMap != null) {
            // 验证所有策略非空
            for (Map.Entry<DesensitizeType, DesensitizeStrategy> entry : strategyMap.entrySet()) {
                if (entry.getKey() == null) {
                    System.err.println("[DESENSITIZE ERROR] Strategy map contains null key");
                    continue;
                }
                if (entry.getValue() == null) {
                    System.err.println("[DESENSITIZE ERROR] Strategy for type " + entry.getKey() + " is null");
                    continue;
                }
            }
            staticStrategyMap = new ConcurrentHashMap<>(strategyMap);
        }
    }

    /**
     * 获取错误计数
     *
     * @return 脱敏失败次数
     */
    public static long getErrorCount() {
        return errorCount.get();
    }

    /**
     * 重置错误计数
     */
    public static void resetErrorCount() {
        errorCount.set(0);
    }

    /**
     * 插件工厂方法
     */
    @PluginFactory
    public static DesensitizePatternLayout createLayout(
            @PluginAttribute(value = "pattern", defaultString = "%m%n") String pattern,
            @PluginConfiguration Configuration config) {

        // 创建委托的 PatternLayout
        PatternLayout.Builder builder = PatternLayout.newBuilder()
            .withPattern(pattern)
            .withConfiguration(config)
            .withCharset(StandardCharsets.UTF_8);

        PatternLayout delegate = builder.build();

        return new DesensitizePatternLayout(config, pattern, delegate);
    }

    private DesensitizePatternLayout(Configuration config, String pattern, PatternLayout delegate) {
        super(StandardCharsets.UTF_8);
        this.delegateLayout = delegate;
    }

    @Override
    public String toSerializable(LogEvent event) {
        try {
            // 使用委托 Layout 格式化
            byte[] bytes = delegateLayout.toByteArray(event);
            if (bytes == null) {
                return "";
            }
            String original = new String(bytes, getCharset());

            // 进行脱敏处理
            return desensitize(original);
        } catch (Exception e) {
            // 格式化失败，返回错误信息
            System.err.println("[DESENSITIZE ERROR] Failed to format log event: " + e.getMessage());
            e.printStackTrace(System.err);
            return "[LOG_FORMAT_ERROR]";
        }
    }

    /**
     * 对消息进行脱敏处理
     *
     * @param message 原始消息
     * @return 脱敏后的消息，如果脱敏失败则返回安全标记
     */
    private String desensitize(String message) {
        // 如果消息为空，直接返回
        if (message == null || message.isEmpty()) {
            return message;
        }

        // 如果配置未初始化或禁用，返回原始消息并记录警告
        if (staticConfig == null) {
            System.err.println("[DESENSITIZE WARNING] Config not initialized, returning original message");
            return message;
        }

        if (!staticConfig.isEnabled()) {
            return message;
        }

        String result = message;
        List<DesensitizeRule> rules = staticConfig.getEnabledRules();

        if (rules == null || rules.isEmpty()) {
            return message;
        }

        // 逐个应用规则，每个规则独立处理异常
        for (DesensitizeRule rule : rules) {
            if (rule == null || !rule.isEnabled()) {
                continue;
            }

            try {
                DesensitizeStrategy strategy = staticStrategyMap.get(rule.getType());
                if (strategy != null) {
                    result = strategy.desensitize(result, rule);
                }
            } catch (Exception e) {
                // 策略执行失败，记录错误并继续处理其他规则
                errorCount.incrementAndGet();
                System.err.println("[DESENSITIZE ERROR] Failed to apply rule " + rule.getType() +
                    ": " + e.getMessage());
                e.printStackTrace(System.err);

                // 为了安全，返回脱敏失败标记而不是原始消息
                // 因为原始消息可能包含敏感信息
                return "[DESENSITIZE_FAILED]";
            }
        }

        return result;
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        return toSerializable(event).getBytes(getCharset());
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }
}
