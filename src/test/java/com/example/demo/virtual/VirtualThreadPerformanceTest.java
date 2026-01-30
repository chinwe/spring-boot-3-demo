package com.example.demo.virtual;

import com.example.demo.virtual.dto.PerformanceComparisonReport;
import com.example.demo.virtual.service.VirtualThreadMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 虚拟线程性能测试
 * 对比传统线程池和虚拟线程在不同场景下的性能表现
 */
@SpringBootTest
class VirtualThreadPerformanceTest {

    @Autowired
    private VirtualThreadMetricsService metricsService;

    @Test
    void testSmallScalePerformance() {
        // 小规模测试：100 个任务
        PerformanceComparisonReport report = metricsService.comparePerformance(100, 10);

        assertNotNull(report);
        assertEquals(100, report.getTaskCount());

        // 验证两种方式都完成了任务
        assertNotNull(report.getTraditionalThreadPool().getTotalDurationMillis());
        assertNotNull(report.getVirtualThreads().getTotalDurationMillis());

        // 验证吞吐量计算正确
        assertTrue(report.getTraditionalThreadPool().getThroughput() > 0);
        assertTrue(report.getVirtualThreads().getThroughput() > 0);
    }

    @Test
    void testMediumScalePerformance() {
        // 中等规模测试：500 个任务
        PerformanceComparisonReport report = metricsService.comparePerformance(500, 5);

        assertNotNull(report);
        assertEquals(500, report.getTaskCount());

        // 虚拟线程应该能处理大量并发
        assertEquals(500, report.getVirtualThreads().getPeakThreads());
    }

    @Test
    void testLargeScalePerformance() {
        // 大规模测试：1000 个任务
        PerformanceComparisonReport report = metricsService.comparePerformance(1000, 5);

        assertNotNull(report);
        assertEquals(1000, report.getTaskCount());

        // 验证虚拟线程的优势
        assertTrue(report.getVirtualThreads().getPeakThreads() >= report.getTraditionalThreadPool().getPeakThreads());
    }

    @Test
    void testShortTaskPerformance() {
        // 短任务测试：每个任务只有 1ms 延迟
        PerformanceComparisonReport report = metricsService.comparePerformance(200, 1);

        assertNotNull(report);
        assertNotNull(report.getTraditionalThreadPool().getTotalDurationMillis());
        assertNotNull(report.getVirtualThreads().getTotalDurationMillis());

        // 短任务场景下，虚拟线程的优势可能不明显
        // 但应该能够完成任务
        assertTrue(report.getVirtualThreads().getThroughput() > 0);
    }

    @Test
    void testLongTaskPerformance() {
        // 长任务测试：每个任务有 50ms 延迟
        PerformanceComparisonReport report = metricsService.comparePerformance(50, 50);

        assertNotNull(report);
        assertEquals(50, report.getTaskCount());

        // 长任务场景下，虚拟线程在高并发时优势更明显
        assertNotNull(report.getImprovementPercentage());
    }

    @Test
    void testMemoryEfficiency() {
        // 测试内存效率
        PerformanceComparisonReport report = metricsService.comparePerformance(500, 10);

        // 虚拟线程的内存占用应该更少
        assertNotNull(report.getTraditionalThreadPool().getMemoryUsageBytes());
        assertNotNull(report.getVirtualThreads().getMemoryUsageBytes());

        // 记录内存使用情况
        System.out.println("Traditional thread pool memory: " +
                report.getTraditionalThreadPool().getMemoryUsageBytes() + " bytes");
        System.out.println("Virtual threads memory: " +
                report.getVirtualThreads().getMemoryUsageBytes() + " bytes");
    }

    @Test
    void testThroughputComparison() {
        // 测试吞吐量对比
        PerformanceComparisonReport report = metricsService.comparePerformance(300, 10);

        double traditionalThroughput = report.getTraditionalThreadPool().getThroughput();
        double virtualThroughput = report.getVirtualThreads().getThroughput();

        // 记录吞吐量数据
        System.out.println("Traditional thread pool throughput: " + traditionalThroughput + " tasks/sec");
        System.out.println("Virtual threads throughput: " + virtualThroughput + " tasks/sec");

        // 两种方式都应该有正的吞吐量
        assertTrue(traditionalThroughput > 0);
        assertTrue(virtualThroughput > 0);
    }

    @Test
    void testScalabilityComparison() {
        // 测试可扩展性对比

        int[] taskCounts = {100, 500, 1000};

        for (int taskCount : taskCounts) {
            PerformanceComparisonReport report = metricsService.comparePerformance(taskCount, 5);

            System.out.println("Task count: " + taskCount);
            System.out.println("  Traditional: " + report.getTraditionalThreadPool().getTotalDurationMillis() + " ms");
            System.out.println("  Virtual: " + report.getVirtualThreads().getTotalDurationMillis() + " ms");
            System.out.println("  Improvement: " + report.getImprovementPercentage() + "%");

            assertNotNull(report);
            assertEquals(taskCount, report.getTaskCount());
        }
    }

    @Test
    void testLatencyComparison() {
        // 测试延迟对比
        PerformanceComparisonReport report = metricsService.comparePerformance(100, 20);

        // 验证平均任务延迟
        assertNotNull(report.getTraditionalThreadPool().getAverageTaskDurationMillis());
        assertNotNull(report.getVirtualThreads().getAverageTaskDurationMillis());

        System.out.println("Average task duration (Traditional): " +
                report.getTraditionalThreadPool().getAverageTaskDurationMillis() + " ms");
        System.out.println("Average task duration (Virtual): " +
                report.getVirtualThreads().getAverageTaskDurationMillis() + " ms");
    }
}
