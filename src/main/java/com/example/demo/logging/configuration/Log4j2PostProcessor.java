package com.example.demo.logging.configuration;

import com.example.demo.logging.desensitize.layout.DesensitizePatternLayout;
import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import com.example.demo.logging.desensitize.strategy.DesensitizeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Log4j2 后处理器
 * 在 Spring 容器初始化完成后，修改 Log4j2 配置以使用脱敏 Layout
 */
@Slf4j
@Component
public class Log4j2PostProcessor {

    private final DesensitizeConfig desensitizeConfig;
    private final Map<DesensitizeType, DesensitizeStrategy> strategyMap;

    @Autowired
    public Log4j2PostProcessor(
            DesensitizeConfig desensitizeConfig,
            Map<DesensitizeType, DesensitizeStrategy> strategyMap) {
        this.desensitizeConfig = desensitizeConfig;
        this.strategyMap = strategyMap;
    }

    /**
     * 应用启动完成后修改 Log4j2 配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void modifyLog4j2Configuration() {
        log.info("Modifying Log4j2 configuration for desensitization...");

        // 设置静态配置
        DesensitizePatternLayout.setStaticConfig(desensitizeConfig);
        DesensitizePatternLayout.setStaticStrategyMap(strategyMap);

        // 获取 LoggerContext
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        // 获取 Root Logger 配置
        LoggerConfig rootLoggerConfig = config.getRootLogger();

        // 获取 Console Appender
        ConsoleAppender consoleAppender = (ConsoleAppender) rootLoggerConfig.getAppenders().get("Console");
        if (consoleAppender != null) {
            // 创建脱敏 Layout
            String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n";
            DesensitizePatternLayout desensitizeLayout = DesensitizePatternLayout.createLayout(pattern, config);

            // 创建新的 Appender
            ConsoleAppender.Builder<?> builder = ConsoleAppender.newBuilder()
                .setConfiguration(config)
                .setName("ConsoleDesensitized")
                .setLayout(desensitizeLayout)
                .setTarget(ConsoleAppender.Target.SYSTEM_OUT);

            ConsoleAppender newAppender = builder.build();

            // 停止旧的 Appender
            consoleAppender.stop();

            // 移除旧的 Appender
            rootLoggerConfig.removeAppender("Console");

            // 添加新的 Appender
            newAppender.start();
            rootLoggerConfig.addAppender(newAppender, null, null);

            // 更新配置
            ctx.updateLoggers();

            log.info("Log4j2 configuration updated with desensitization layout");
        }

        log.info("Desensitization configured with {} rules, enabled={}",
            desensitizeConfig.getRules() != null ? desensitizeConfig.getRules().size() : 0,
            desensitizeConfig.isEnabled());
    }
}
