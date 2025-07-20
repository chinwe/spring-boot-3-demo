package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AsyncMetricsService {

    @Resource(name = "asyncTaskExecutor")
    private Executor asyncTaskExecutor;

    private final AtomicLong totalAsyncTasks = new AtomicLong(0);
    private final AtomicLong completedAsyncTasks = new AtomicLong(0);
    private final AtomicLong failedAsyncTasks = new AtomicLong(0);

    public void incrementTotalTasks() {
        long total = totalAsyncTasks.incrementAndGet();
        log.debug("Total async tasks: {}", total);
    }

    public void incrementCompletedTasks() {
        long completed = completedAsyncTasks.incrementAndGet();
        log.debug("Completed async tasks: {}", completed);
    }

    public void incrementFailedTasks() {
        long failed = failedAsyncTasks.incrementAndGet();
        log.debug("Failed async tasks: {}", failed);
    }

    public Map<String, Object> getAsyncMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Task metrics
        metrics.put("totalTasks", totalAsyncTasks.get());
        metrics.put("completedTasks", completedAsyncTasks.get());
        metrics.put("failedTasks", failedAsyncTasks.get());
        
        // Thread pool metrics
        if (asyncTaskExecutor instanceof ThreadPoolTaskExecutor executor) {
            metrics.put("threadPool", getThreadPoolMetrics(executor));
        }
        
        log.info("Current async metrics: {}", metrics);
        return metrics;
    }

    private Map<String, Object> getThreadPoolMetrics(ThreadPoolTaskExecutor executor) {
        Map<String, Object> threadPoolMetrics = new HashMap<>();
        
        threadPoolMetrics.put("corePoolSize", executor.getCorePoolSize());
        threadPoolMetrics.put("maxPoolSize", executor.getMaxPoolSize());
        threadPoolMetrics.put("activeCount", executor.getActiveCount());
        threadPoolMetrics.put("poolSize", executor.getPoolSize());
        threadPoolMetrics.put("queueSize", executor.getThreadPoolExecutor().getQueue().size());
        threadPoolMetrics.put("completedTaskCount", executor.getThreadPoolExecutor().getCompletedTaskCount());
        threadPoolMetrics.put("taskCount", executor.getThreadPoolExecutor().getTaskCount());
        
        return threadPoolMetrics;
    }

    public void logPerformanceMetrics(String operation, long startTime, long endTime) {
        long duration = endTime - startTime;
        log.info("Performance metrics - Operation: {}, Duration: {}ms, Start: {}, End: {}", 
                operation, duration, startTime, endTime);
        
        if (duration > 10000) { // Log warning for operations taking more than 10 seconds
            log.warn("Slow async operation detected - Operation: {}, Duration: {}ms", operation, duration);
        }
    }

    public void resetMetrics() {
        totalAsyncTasks.set(0);
        completedAsyncTasks.set(0);
        failedAsyncTasks.set(0);
        log.info("Async metrics reset");
    }
}