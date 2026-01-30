package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.PerformanceComparisonReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 虚拟线程指标服务测试
 */
class VirtualThreadMetricsServiceTest {

    private VirtualThreadMetricsService service;

    @BeforeEach
    void setUp() {
        service = new VirtualThreadMetricsService();
    }

    @Test
    void testComparePerformance() {
        PerformanceComparisonReport report = service.comparePerformance(100, 10);

        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertNotNull(report.getTestTime());
        assertEquals(100, report.getTaskCount());
        assertNotNull(report.getTraditionalThreadPool());
        assertNotNull(report.getVirtualThreads());
    }

    @Test
    void testPerformanceWithSmallTaskCount() {
        PerformanceComparisonReport report = service.comparePerformance(10, 5);

        assertNotNull(report);
        assertEquals(10, report.getTaskCount());
        assertNotNull(report.getTraditionalThreadPool().getTotalDurationMillis());
        assertNotNull(report.getVirtualThreads().getTotalDurationMillis());
    }

    @Test
    void testPerformanceWithLargeTaskCount() {
        PerformanceComparisonReport report = service.comparePerformance(500, 5);

        assertNotNull(report);
        assertEquals(500, report.getTaskCount());
        assertNotNull(report.getTraditionalThreadPool().getTotalDurationMillis());
        assertNotNull(report.getVirtualThreads().getTotalDurationMillis());
    }

    @Test
    void testPerformanceMetricsArePresent() {
        PerformanceComparisonReport report = service.comparePerformance(50, 10);

        PerformanceComparisonReport.PerformanceResult traditional = report.getTraditionalThreadPool();
        PerformanceComparisonReport.PerformanceResult virtual = report.getVirtualThreads();

        // 验证传统线程池指标
        assertNotNull(traditional.getTotalDurationMillis());
        assertNotNull(traditional.getAverageTaskDurationMillis());
        assertNotNull(traditional.getThroughput());
        assertNotNull(traditional.getPeakThreads());
        assertNotNull(traditional.getMemoryUsageBytes());

        // 验证虚拟线程指标
        assertNotNull(virtual.getTotalDurationMillis());
        assertNotNull(virtual.getAverageTaskDurationMillis());
        assertNotNull(virtual.getThroughput());
        assertNotNull(virtual.getPeakThreads());
        assertNotNull(virtual.getMemoryUsageBytes());
    }

    @Test
    void testVirtualThreadsHaveHigherPeakThreads() {
        PerformanceComparisonReport report = service.comparePerformance(100, 10);

        // 虚拟线程的峰值线程数应该等于或接近任务数
        // 而传统线程池受限于线程池大小
        assertTrue(report.getVirtualThreads().getPeakThreads() >= report.getTraditionalThreadPool().getPeakThreads());
    }

    @Test
    void testImprovementPercentageIsCalculated() {
        PerformanceComparisonReport report = service.comparePerformance(100, 10);

        // 改进百分比可能为正、零或负
        assertNotNull(report.getImprovementPercentage());
    }

    @Test
    void testThroughputIsCalculated() {
        PerformanceComparisonReport report = service.comparePerformance(50, 10);

        assertTrue(report.getTraditionalThreadPool().getThroughput() > 0);
        assertTrue(report.getVirtualThreads().getThroughput() > 0);
    }
}
