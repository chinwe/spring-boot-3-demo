package com.example.demo;

import com.example.demo.metrics.binder.AsyncMetrics;
import com.example.demo.metrics.binder.JooqMetrics;
import com.example.demo.metrics.binder.RetryMetrics;
import com.example.demo.metrics.binder.SentinelMetrics;
import com.example.demo.metrics.binder.VirtualThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Micrometer 和 Prometheus 端点集成测试
 * 验证指标收集和端点暴露功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MetricsIntegrationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private TestRestTemplate restTemplate;

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
     * 测试 Prometheus 端点可访问性
     */
    @Test
    void testPrometheusEndpointAccessible() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * 测试 Prometheus 端点返回正确的内容类型
     */
    @Test
    void testPrometheusEndpointContentType() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);
        assertThat(response.getHeaders().getContentType().toString()).contains("text/plain");
    }

    /**
     * 测试 Prometheus 端点包含 JVM 指标
     */
    @Test
    void testJvmMetricsPresentInPrometheus() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);
        String body = response.getBody();

        // 验证包含 JVM 指标
        assertThat(body).contains("jvm_memory");
        assertThat(body).contains("application=");
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
}
