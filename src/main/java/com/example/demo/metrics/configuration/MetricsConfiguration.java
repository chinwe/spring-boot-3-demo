package com.example.demo.metrics.configuration;

import com.example.demo.metrics.binder.AsyncMetrics;
import com.example.demo.metrics.binder.JooqMetrics;
import com.example.demo.metrics.binder.RetryMetrics;
import com.example.demo.metrics.binder.SentinelMetrics;
import com.example.demo.metrics.binder.VirtualThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;

/**
 * Micrometer 指标配置类
 * 配置 JVM 绑定器和自定义指标
 */
@Slf4j
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class MetricsConfiguration {

    public MetricsConfiguration() {
        log.info("MetricsConfiguration initialized");
    }

    /**
     * JVM 内存指标绑定器（自动配置，此处显式声明）
     * Spring Boot Actuator 默认自动绑定，无需手动配置
     */
    // JvmMemoryMetrics 通过 Micrometer 自动配置启用

    /**
     * JVM GC 指标绑定器
     */
    // JvmGcMetrics 通过 Micrometer 自动配置启用

    /**
     * JVM 线程指标绑定器
     */
    // JvmThreadMetrics 通过 Micrometer 自动配置启用

    /**
     * 处理器指标绑定器
     */
    // ProcessorMetrics 通过 Micrometer 自动配置启用

    /**
     * Logback 指标绑定器
     * 在 application.properties 中通过 management.metrics.enable.logback=true 启用
     */
    // LogbackMetrics 通过 Micrometer 自动配置启用

    /**
     * 自定义指标 Bean 已经通过 @Component 注解自动注册
     * 包括：
     * - AsyncMetrics
     * - RetryMetrics
     * - JooqMetrics
     * - VirtualThreadMetrics
     * - SentinelMetrics
     *
     * 它们会自动注入 MeterRegistry 并注册指标
     */
}
