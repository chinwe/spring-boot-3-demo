package com.example.demo.metrics.binder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 异步任务指标收集器
 * 使用 Micrometer 记录异步任务的执行情况
 */
@Slf4j
@Component
public class AsyncMetrics {

    private final Counter taskSubmitted;
    private final Counter taskRunning;
    private final Counter taskCompleted;
    private final Counter taskFailed;
    private final Timer taskDuration;

    public AsyncMetrics(MeterRegistry registry) {
        this.taskSubmitted = Counter.builder("async_tasks_total")
                .description("Total number of async tasks submitted")
                .tag("status", "submitted")
                .register(registry);

        this.taskRunning = Counter.builder("async_tasks_total")
                .description("Total number of async tasks running")
                .tag("status", "running")
                .register(registry);

        this.taskCompleted = Counter.builder("async_tasks_total")
                .description("Total number of async tasks completed")
                .tag("status", "completed")
                .register(registry);

        this.taskFailed = Counter.builder("async_tasks_total")
                .description("Total number of async tasks failed")
                .tag("status", "failed")
                .register(registry);

        this.taskDuration = Timer.builder("async_task_duration_seconds")
                .description("Async task execution duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        log.info("AsyncMetrics initialized with Micrometer registry");
    }

    /**
     * 记录任务提交
     */
    public void recordTaskSubmitted() {
        taskSubmitted.increment();
        log.debug("Async task submitted metric recorded");
    }

    /**
     * 记录任务开始运行
     */
    public void recordTaskRunning() {
        taskRunning.increment();
        log.debug("Async task running metric recorded");
    }

    /**
     * 记录任务完成
     */
    public void recordTaskCompleted() {
        taskCompleted.increment();
        log.debug("Async task completed metric recorded");
    }

    /**
     * 记录任务失败
     */
    public void recordTaskFailed() {
        taskFailed.increment();
        log.debug("Async task failed metric recorded");
    }

    /**
     * 记录任务执行时长
     */
    public void recordTaskDuration(long durationMillis) {
        taskDuration.record(durationMillis, TimeUnit.MILLISECONDS);
        log.debug("Async task duration metric recorded: {}ms", durationMillis);
    }

    /**
     * 使用 Timer.Sample 记录任务执行时长（推荐方式）
     */
    public Timer.Sample startTimer() {
        return Timer.start();
    }

    /**
     * 停止计时并记录
     */
    public void stopTimer(Timer.Sample sample) {
        sample.stop(taskDuration);
    }
}
