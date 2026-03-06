package com.example.demo.metrics.binder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 虚拟线程指标收集器
 * 使用 Micrometer 记录虚拟线程的使用情况和 Pin 状态
 */
@Slf4j
@Component
public class VirtualThreadMetrics {

    private final Counter pinCounter;
    private final AtomicLong pinnedThreadCount;
    private final ThreadMXBean threadMXBean;

    public VirtualThreadMetrics(MeterRegistry registry) {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.pinnedThreadCount = new AtomicLong(0);

        // Pin 检测计数器
        this.pinCounter = Counter.builder("virtual_thread_pins_total")
                .description("Total number of virtual thread pin events detected")
                .register(registry);

        // 虚拟线程数量 Gauge（动态测量）
        Gauge.builder("virtual_thread_live_count", this, VirtualThreadMetrics::getVirtualThreadCount)
                .description("Current number of live virtual threads")
                .tag("type", "virtual")
                .register(registry);

        // Carrier 线程数量 Gauge
        Gauge.builder("carrier_thread_live_count", this, VirtualThreadMetrics::getCarrierThreadCount)
                .description("Current number of live carrier threads")
                .tag("type", "carrier")
                .register(registry);

        // Pinned 线程数量 Gauge
        Gauge.builder("virtual_thread_pinned_count", pinnedThreadCount, AtomicLong::get)
                .description("Current number of pinned virtual threads")
                .register(registry);

        log.info("VirtualThreadMetrics initialized with Micrometer registry");
    }

    /**
     * 记录虚拟线程 Pin 事件
     */
    public void recordPinEvent(String reason) {
        pinCounter.increment();
        pinnedThreadCount.incrementAndGet();
        log.debug("Virtual thread pin detected - reason: {}", reason);
    }

    /**
     * 清除 Pin 状态（当虚拟线程解除固定时调用）
     */
    public void clearPinState() {
        pinnedThreadCount.decrementAndGet();
        log.debug("Virtual thread pin state cleared");
    }

    /**
     * 获取当前虚拟线程数量（估算）
     */
    private double getVirtualThreadCount() {
        try {
            // Java 21+ 提供了获取虚拟线程数量的方法
            // 这里使用传统方式估算
            int threadCount = threadMXBean.getThreadCount();
            // 虚拟线程数量 = 总线程数 - 平台线程数
            // 平台线程数通常等于处理器数量的倍数
            int platformThreads = Runtime.getRuntime().availableProcessors() * 2;
            return Math.max(0, threadCount - platformThreads);
        } catch (Exception e) {
            log.warn("Failed to get virtual thread count", e);
            return 0;
        }
    }

    /**
     * 获取 Carrier 线程数量
     */
    private double getCarrierThreadCount() {
        try {
            // ForkJoinPool 的默认并行度等于处理器数量
            return Runtime.getRuntime().availableProcessors();
        } catch (Exception e) {
            log.warn("Failed to get carrier thread count", e);
            return Runtime.getRuntime().availableProcessors();
        }
    }

    /**
     * 获取当前 Pinned 线程数量
     */
    public long getPinnedThreadCount() {
        return pinnedThreadCount.get();
    }

    /**
     * 检查当前线程是否为虚拟线程
     */
    public static boolean isCurrentThreadVirtual() {
        return Thread.currentThread().isVirtual();
    }
}
