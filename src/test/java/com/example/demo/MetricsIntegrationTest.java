package com.example.demo;

import com.example.demo.metrics.binder.AsyncMetrics;
import com.example.demo.metrics.binder.JooqMetrics;
import com.example.demo.metrics.binder.RetryMetrics;
import com.example.demo.metrics.binder.SentinelMetrics;
import com.example.demo.metrics.binder.VirtualThreadMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Micrometer 和 Prometheus 指标集成测试
 * 验证指标收集和配置功能
 */
@SpringBootTest
@ActiveProfiles("test")
class MetricsIntegrationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MetricsProperties metricsProperties;

    @Autowired(required = false)
    private AsyncMetrics asyncMetrics;

    @Autowired(required = false)
    private RetryMetrics retryMetrics;

    @Autowired(required = false)
    private JooqMetrics jooqMetrics;

    @Autowired(required = false)
    private VirtualThreadMetrics virtualThreadMetrics;

    @Autowired(required = false)
    private SentinelMetrics sentinelMetrics;

    /**
     * 测试 MeterRegistry 正确配置
     */
    @Test
    void testMeterRegistryConfigured() {
        assertThat(meterRegistry).isNotNull();
    }

    /**
     * 测试直方图配置已启用
     */
    @Test
    void testHttpServerRequestsHistogramEnabled() {
        Boolean histogramEnabled = metricsProperties.getDistribution()
                .getPercentilesHistogram()
                .get("http.server.requests");
        assertThat(histogramEnabled).isTrue();
    }

    /**
     * 测试百分位数配置保持不变
     */
    @Test
    void testHttpServerRequestsPercentilesConfigured() {
        var percentiles = metricsProperties.getDistribution()
                .getPercentiles()
                .get("http.server.requests");
        assertThat(percentiles).isNotNull();
        assertThat(percentiles).containsExactly(0.5, 0.95, 0.99);
    }

    /**
     * 测试自定义指标 Bean 已创建
     */
    @Test
    void testCustomMetricsBeansCreated() {
        // 验证自定义指标 Bean 已被 Spring 容器管理
        assertThat(asyncMetrics).isNotNull();
        assertThat(retryMetrics).isNotNull();
        assertThat(jooqMetrics).isNotNull();
        assertThat(virtualThreadMetrics).isNotNull();
        assertThat(sentinelMetrics).isNotNull();
    }

    /**
     * 测试自定义异步指标已注册
     */
    @Test
    void testAsyncMetricsRegistered() {
        // 记录一些指标来触发注册
        asyncMetrics.recordTaskSubmitted();
        asyncMetrics.recordTaskCompleted();

        // 验证指标已注册
        assertThat(meterRegistry.getMeters().stream()
                .anyMatch(m -> m.getId().getName().contains("async_tasks")))
                .isTrue();
    }

    /**
     * 测试自定义重试指标已注册
     */
    @Test
    void testRetryMetricsRegistered() {
        // 记录一些指标来触发注册
        retryMetrics.recordRetryDuration(100);

        // 验证指标已注册
        assertThat(meterRegistry.getMeters().stream()
                .anyMatch(m -> m.getId().getName().contains("retry_duration")))
                .isTrue();
    }

    /**
     * 测试 JOOQ 指标已注册
     */
    @Test
    void testJooqMetricsRegistered() {
        // 记录一些指标来触发注册
        jooqMetrics.recordQueryDuration(100, "select", "test_table");

        // 验证指标已注册
        assertThat(meterRegistry.getMeters().stream()
                .anyMatch(m -> m.getId().getName().contains("jooq_query")))
                .isTrue();
    }

    // ========== HTTP 直方图指标测试 ==========

    /**
     * 测试 HTTP 请求直方图配置已启用
     */
    @Test
    void testHttpServerRequestsHistogramConfigurationEnabled() {
        Boolean histogramEnabled = metricsProperties.getDistribution()
                .getPercentilesHistogram()
                .get("http.server.requests");
        assertThat(histogramEnabled).isTrue();
    }

    /**
     * 测试 HTTP 请求百分位数配置
     */
    @Test
    void testHttpServerRequestsPercentilesConfiguration() {
        var percentiles = metricsProperties.getDistribution()
                .getPercentiles()
                .get("http.server.requests");
        assertThat(percentiles).isNotNull();
        assertThat(percentiles).containsExactly(0.5, 0.95, 0.99);
    }

    /**
     * 测试 JVM 指标已注册
     */
    @Test
    void testJvmMetricsRegistered() {
        assertThat(meterRegistry.find("jvm.memory.used").gauge()).isNotNull();
        assertThat(meterRegistry.find("jvm.gc.pause").timer()).isNotNull();
    }

    /**
     * 测试应用标签已配置
     */
    @Test
    void testApplicationTagsConfigured() {
        var tags = metricsProperties.getTags();
        assertThat(tags).containsKey("application");
        assertThat(tags.get("application")).isEqualTo("demo-test");
        assertThat(tags.get("environment")).isEqualTo("test");
    }
}
