package com.example.demo.metrics.binder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 重试指标收集器
 * 使用 Micrometer 记录重试操作的执行情况
 */
@Slf4j
@Component
public class RetryMetrics {

    private final MeterRegistry registry;
    private final Timer retryDuration;

    public RetryMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.retryDuration = Timer.builder("retry_duration_seconds")
                .description("Total retry operation duration including backoff")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        log.info("RetryMetrics initialized with Micrometer registry");
    }

    /**
     * 记录重试尝试
     */
    public void recordRetryAttempt(String serviceName, String methodName, String outcome) {
        Counter.builder("retry_attempts_total")
                .description("Total number of retry attempts")
                .tag("service", serviceName)
                .tag("method", methodName)
                .tag("outcome", outcome)
                .register(registry)
                .increment();

        log.debug("Retry attempt recorded - service: {}, method: {}, outcome: {}",
                serviceName, methodName, outcome);
    }

    /**
     * 记录成功结果
     */
    public void recordSuccess(String serviceName, String methodName) {
        Counter.builder("retry_outcomes_total")
                .description("Total number of retry outcomes")
                .tag("service", serviceName)
                .tag("method", methodName)
                .tag("result", "success")
                .register(registry)
                .increment();

        log.debug("Retry success recorded - service: {}, method: {}", serviceName, methodName);
    }

    /**
     * 记录失败结果（达到最大重试次数）
     */
    public void recordMaxRetriesReached(String serviceName, String methodName) {
        Counter.builder("retry_outcomes_total")
                .description("Total number of retry outcomes")
                .tag("service", serviceName)
                .tag("method", methodName)
                .tag("result", "max_reached")
                .register(registry)
                .increment();

        log.debug("Retry max reached recorded - service: {}, method: {}", serviceName, methodName);
    }

    /**
     * 记录重试总时长（包括退避延迟）
     */
    public void recordRetryDuration(long durationMillis) {
        retryDuration.record(durationMillis, TimeUnit.MILLISECONDS);
        log.debug("Retry duration recorded: {}ms", durationMillis);
    }

    /**
     * 使用 Timer.Sample 记录重试时长
     */
    public Timer.Sample startTimer() {
        return Timer.start();
    }

    /**
     * 停止计时并记录
     */
    public void stopTimer(Timer.Sample sample) {
        sample.stop(retryDuration);
    }
}
