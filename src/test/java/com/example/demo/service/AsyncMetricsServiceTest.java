package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.demo.service.AsyncMetricsService;

/**
 * AsyncMetricsService 单元测试
 * 验证异步指标收集服务的功能正确性
 *
 * @author chinwe
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("异步指标服务单元测试")
class AsyncMetricsServiceTest {

    private AsyncMetricsService metricsService;

    @Mock
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @BeforeEach
    void setUp() {
        metricsService = new AsyncMetricsService();

        // 使用反射设置 asyncTaskExecutor
        try {
            Field field = AsyncMetricsService.class.getDeclaredField("asyncTaskExecutor");
            field.setAccessible(true);
            field.set(metricsService, asyncTaskExecutor);

            // 默认配置 mock executor 以避免 NullPointerException
            var threadPoolExecutor = mock(java.util.concurrent.ThreadPoolExecutor.class);
            when(threadPoolExecutor.getQueue()).thenReturn(new java.util.concurrent.LinkedBlockingQueue<>());
            when(asyncTaskExecutor.getThreadPoolExecutor()).thenReturn(threadPoolExecutor);
        } catch (Exception e) {
            // Ignore for tests that don't need executor
        }

        // 重置指标
        metricsService.resetMetrics();
    }

    @Test
    @DisplayName("总任务数递增")
    void testIncrementTotalTasks() throws Exception {
        // Given
        Field field = AsyncMetricsService.class.getDeclaredField("totalAsyncTasks");
        field.setAccessible(true);
        AtomicLong totalTasks = (AtomicLong) field.get(metricsService);
        long initialValue = totalTasks.get();

        // When
        metricsService.incrementTotalTasks();

        // Then
        assertEquals(initialValue + 1, totalTasks.get());
    }

    @Test
    @DisplayName("完成任务数递增")
    void testIncrementCompletedTasks() throws Exception {
        // Given
        Field field = AsyncMetricsService.class.getDeclaredField("completedAsyncTasks");
        field.setAccessible(true);
        AtomicLong completedTasks = (AtomicLong) field.get(metricsService);
        long initialValue = completedTasks.get();

        // When
        metricsService.incrementCompletedTasks();

        // Then
        assertEquals(initialValue + 1, completedTasks.get());
    }

    @Test
    @DisplayName("失败任务数递增")
    void testIncrementFailedTasks() throws Exception {
        // Given
        Field field = AsyncMetricsService.class.getDeclaredField("failedAsyncTasks");
        field.setAccessible(true);
        AtomicLong failedTasks = (AtomicLong) field.get(metricsService);
        long initialValue = failedTasks.get();

        // When
        metricsService.incrementFailedTasks();

        // Then
        assertEquals(initialValue + 1, failedTasks.get());
    }

    @Test
    @DisplayName("并发递增线程安全")
    void testConcurrentIncrement() throws Exception {
        // Given
        Field field = AsyncMetricsService.class.getDeclaredField("totalAsyncTasks");
        field.setAccessible(true);
        AtomicLong totalTasks = (AtomicLong) field.get(metricsService);

        // When - 多线程并发递增
        Runnable incrementTask = () -> {
            for (int i = 0; i < 100; i++) {
                metricsService.incrementTotalTasks();
            }
        };

        Thread thread1 = new Thread(incrementTask);
        Thread thread2 = new Thread(incrementTask);
        Thread thread3 = new Thread(incrementTask);

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join(1000);
        thread2.join(1000);
        thread3.join(1000);

        // Then - 验证最终值正确
        assertEquals(300, totalTasks.get());
    }

    @Test
    @DisplayName("初始值为0")
    void testGetAsyncMetrics_InitialValues() {
        // When
        Map<String, Object> metrics = metricsService.getAsyncMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("totalTasks"));
        assertEquals(0L, metrics.get("completedTasks"));
        assertEquals(0L, metrics.get("failedTasks"));
    }

    @Test
    @DisplayName("包含线程池指标")
    void testGetAsyncMetrics_WithThreadPoolMetrics() {
        // Given - 配置 mock executor
        when(asyncTaskExecutor.getCorePoolSize()).thenReturn(5);
        when(asyncTaskExecutor.getMaxPoolSize()).thenReturn(20);
        when(asyncTaskExecutor.getActiveCount()).thenReturn(2);
        when(asyncTaskExecutor.getPoolSize()).thenReturn(5);

        var threadPoolExecutor = mock(java.util.concurrent.ThreadPoolExecutor.class);
        java.util.concurrent.BlockingQueue<Runnable> queue = new java.util.concurrent.LinkedBlockingQueue<>();
        queue.add(() -> {}); // 添加一个元素使队列不为空
        when(asyncTaskExecutor.getThreadPoolExecutor()).thenReturn(threadPoolExecutor);
        when(threadPoolExecutor.getQueue()).thenReturn(queue);
        when(threadPoolExecutor.getCompletedTaskCount()).thenReturn(100L);
        when(threadPoolExecutor.getTaskCount()).thenReturn(150L);

        // When
        Map<String, Object> metrics = metricsService.getAsyncMetrics();

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("threadPool"));

        @SuppressWarnings("unchecked")
        Map<String, Object> threadPoolMetrics = (Map<String, Object>) metrics.get("threadPool");
        assertNotNull(threadPoolMetrics);
        assertEquals(5, threadPoolMetrics.get("corePoolSize"));
        assertEquals(20, threadPoolMetrics.get("maxPoolSize"));
        assertEquals(2, threadPoolMetrics.get("activeCount"));
        assertEquals(5, threadPoolMetrics.get("poolSize"));
        assertEquals(1, threadPoolMetrics.get("queueSize"));
        assertEquals(100L, threadPoolMetrics.get("completedTaskCount"));
        assertEquals(150L, threadPoolMetrics.get("taskCount"));
    }

    @Test
    @DisplayName("Executor非ThreadPoolTaskExecutor类型")
    void testGetAsyncMetrics_WithoutThreadPoolExecutor() throws Exception {
        // Given - 使用普通 Executor
        Field field = AsyncMetricsService.class.getDeclaredField("asyncTaskExecutor");
        field.setAccessible(true);
        field.set(metricsService, (Executor) Runnable -> new Thread(Runnable).start());

        // When
        Map<String, Object> metrics = metricsService.getAsyncMetrics();

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalTasks"));
        assertTrue(metrics.containsKey("completedTasks"));
        assertTrue(metrics.containsKey("failedTasks"));
        // 不应包含 threadPool 指标
        assertTrue(!metrics.containsKey("threadPool") || metrics.get("threadPool") == null);
    }

    @Test
    @DisplayName("正常操作日志")
    void testLogPerformanceMetrics_NormalOperation() {
        // Given
        long startTime = System.currentTimeMillis();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();

        // When & Then - 不应抛出异常
        metricsService.logPerformanceMetrics("testOperation", startTime, endTime);
    }

    @Test
    @DisplayName("慢操作警告（>10秒）")
    void testLogPerformanceMetrics_SlowOperation() {
        // Given
        long startTime = System.currentTimeMillis() - 11000; // 11秒前
        long endTime = System.currentTimeMillis();

        // When & Then - 不应抛出异常
        metricsService.logPerformanceMetrics("slowOperation", startTime, endTime);
    }

    @Test
    @DisplayName("重置所有指标")
    void testResetMetrics() throws Exception {
        // Given - 先增加一些指标
        metricsService.incrementTotalTasks();
        metricsService.incrementCompletedTasks();
        metricsService.incrementFailedTasks();

        Field totalField = AsyncMetricsService.class.getDeclaredField("totalAsyncTasks");
        Field completedField = AsyncMetricsService.class.getDeclaredField("completedAsyncTasks");
        Field failedField = AsyncMetricsService.class.getDeclaredField("failedAsyncTasks");
        totalField.setAccessible(true);
        completedField.setAccessible(true);
        failedField.setAccessible(true);

        AtomicLong totalTasks = (AtomicLong) totalField.get(metricsService);
        AtomicLong completedTasks = (AtomicLong) completedField.get(metricsService);
        AtomicLong failedTasks = (AtomicLong) failedField.get(metricsService);

        assertTrue(totalTasks.get() > 0);
        assertTrue(completedTasks.get() > 0);
        assertTrue(failedTasks.get() > 0);

        // When
        metricsService.resetMetrics();

        // Then - 所有指标归零
        assertEquals(0, totalTasks.get());
        assertEquals(0, completedTasks.get());
        assertEquals(0, failedTasks.get());
    }

    @Test
    @DisplayName("完整工作流")
    void testMetricsWorkflow() {
        // Given
        metricsService.resetMetrics();

        // When - 模拟完整工作流
        metricsService.incrementTotalTasks();
        long startTime = System.currentTimeMillis();
        metricsService.incrementCompletedTasks();
        long endTime = System.currentTimeMillis();
        metricsService.logPerformanceMetrics("workflowTest", startTime, endTime);

        Map<String, Object> metrics = metricsService.getAsyncMetrics();

        // Then
        assertEquals(1L, metrics.get("totalTasks"));
        assertEquals(1L, metrics.get("completedTasks"));
        assertEquals(0L, metrics.get("failedTasks"));
    }
}
