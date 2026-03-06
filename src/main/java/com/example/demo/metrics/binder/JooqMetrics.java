package com.example.demo.metrics.binder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * JOOQ 数据库查询指标收集器
 * 使用 Micrometer 记录数据库操作情况
 */
@Slf4j
@Component
public class JooqMetrics {

    private final Counter queryCounter;
    private final Timer queryTimer;
    private final Counter slowQueryCounter;

    // 慢查询阈值（毫秒）
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;

    public JooqMetrics(MeterRegistry registry) {
        this.queryCounter = Counter.builder("jooq_queries_total")
                .description("Total number of JOOQ queries executed")
                .register(registry);

        this.queryTimer = Timer.builder("jooq_query_duration_seconds")
                .description("JOOQ query execution duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.slowQueryCounter = Counter.builder("jooq_slow_queries_total")
                .description("Total number of slow JOOQ queries")
                .register(registry);

        log.info("JooqMetrics initialized with Micrometer registry");
    }

    /**
     * 记录查询执行
     */
    public void recordQuery(String operation, String table) {
        queryCounter.increment();
        log.debug("JOOQ query recorded - operation: {}, table: {}", operation, table);
    }

    /**
     * 记录查询执行时长
     */
    public void recordQueryDuration(long durationMillis, String operation, String table) {
        queryTimer.record(durationMillis, TimeUnit.MILLISECONDS);

        // 检测慢查询
        if (durationMillis > SLOW_QUERY_THRESHOLD_MS) {
            slowQueryCounter.increment();
            log.warn("Slow JOOQ query detected - operation: {}, table: {}, duration: {}ms",
                    operation, table, durationMillis);
        }

        log.debug("JOOQ query duration recorded - operation: {}, table: {}, duration: {}ms",
                operation, table, durationMillis);
    }

    /**
     * 使用 Timer.Sample 记录查询时长（推荐方式）
     */
    public Timer.Sample startTimer() {
        return Timer.start();
    }

    /**
     * 停止计时并记录查询
     */
    public void stopTimer(Timer.Sample sample, String operation, String table) {
        long duration = sample.stop(queryTimer);
        recordQuery(operation, table);
        log.debug("JOOQ query completed - operation: {}, table: {}, duration: {}ms",
                operation, table, duration / 1_000_000);
    }

    /**
     * 获取慢查询阈值
     */
    public long getSlowQueryThreshold() {
        return SLOW_QUERY_THRESHOLD_MS;
    }
}
