package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.PerformanceComparisonReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 虚拟线程指标服务
 * 对比传统线程池和虚拟线程的性能
 */
@Service
@Slf4j
public class VirtualThreadMetricsService {

    /**
     * 性能对比测试
     */
    public PerformanceComparisonReport comparePerformance(int taskCount, int taskDelayMillis) {
        String reportId = UUID.randomUUID().toString();
        LocalDateTime testTime = LocalDateTime.now();

        log.info("=== Starting performance comparison ===");
        log.info("Task count: {}, Task delay: {} ms", taskCount, taskDelayMillis);

        // 测试传统线程池
        PerformanceComparisonReport.PerformanceResult traditionalResult =
                measureTraditionalThreadPool(taskCount, taskDelayMillis);

        // 测试虚拟线程
        PerformanceComparisonReport.PerformanceResult virtualResult =
                measureVirtualThreads(taskCount, taskDelayMillis);

        // 计算性能提升
        double improvement = 0;
        if (traditionalResult.getTotalDurationMillis() != null && virtualResult.getTotalDurationMillis() != null) {
            improvement = ((double) (traditionalResult.getTotalDurationMillis() - virtualResult.getTotalDurationMillis())
                    / traditionalResult.getTotalDurationMillis()) * 100;
        }

        PerformanceComparisonReport report = PerformanceComparisonReport.builder()
                .reportId(reportId)
                .testTime(testTime)
                .taskCount(taskCount)
                .traditionalThreadPool(traditionalResult)
                .virtualThreads(virtualResult)
                .improvementPercentage(improvement)
                .build();

        log.info("=== Performance comparison completed ===");
        log.info("Traditional thread pool: {} ms", traditionalResult.getTotalDurationMillis());
        log.info("Virtual threads: {} ms", virtualResult.getTotalDurationMillis());
        log.info("Improvement: {:.2f}%", improvement);

        return report;
    }

    /**
     * 测量传统线程池性能
     */
    private PerformanceComparisonReport.PerformanceResult measureTraditionalThreadPool(
            int taskCount, int taskDelayMillis) {

        log.info("Measuring traditional thread pool performance...");

        int poolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger peakThreads = new AtomicInteger(0);
        AtomicInteger currentThreads = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            final int taskIndex = i;
            Future<?> future = threadPool.submit(() -> {
                int current = currentThreads.incrementAndGet();
                int peak;
                while ((peak = peakThreads.get()) < current && !peakThreads.compareAndSet(peak, current)) {
                    // CAS loop
                }

                try {
                    Thread.sleep(taskDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    currentThreads.decrementAndGet();
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long totalDuration = endTime - startTime;
        long memoryUsage = endMemory - startMemory;

        return PerformanceComparisonReport.PerformanceResult.builder()
                .totalDurationMillis(totalDuration)
                .averageTaskDurationMillis((double) totalDuration / taskCount)
                .throughput(taskCount * 1000.0 / totalDuration)
                .peakThreads(peakThreads.get())
                .memoryUsageBytes(memoryUsage)
                .build();
    }

    /**
     * 测量虚拟线程性能
     */
    private PerformanceComparisonReport.PerformanceResult measureVirtualThreads(
            int taskCount, int taskDelayMillis) {

        log.info("Measuring virtual threads performance...");

        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(taskCount);

        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        for (int i = 0; i < taskCount; i++) {
            virtualExecutor.submit(() -> {
                try {
                    Thread.sleep(taskDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        virtualExecutor.close();
        try {
            virtualExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long totalDuration = endTime - startTime;
        long memoryUsage = endMemory - startMemory;

        return PerformanceComparisonReport.PerformanceResult.builder()
                .totalDurationMillis(totalDuration)
                .averageTaskDurationMillis((double) totalDuration / taskCount)
                .throughput(taskCount * 1000.0 / totalDuration)
                .peakThreads(taskCount) // 虚拟线程可以有任意数量
                .memoryUsageBytes(memoryUsage)
                .build();
    }
}
