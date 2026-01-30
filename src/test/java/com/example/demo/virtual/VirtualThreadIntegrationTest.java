package com.example.demo.virtual;

import com.example.demo.virtual.controller.VirtualThreadController;
import com.example.demo.virtual.dto.PerformanceComparisonReport;
import com.example.demo.virtual.dto.PinDetectionReport;
import com.example.demo.virtual.dto.StructuredConcurrencyResult;
import com.example.demo.virtual.dto.VirtualThreadTaskDto;
import com.example.demo.virtual.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 虚拟线程集成测试
 * 测试虚拟线程模块的端到端功能
 */
@SpringBootTest
class VirtualThreadIntegrationTest {

    @Autowired
    private VirtualThreadController virtualThreadController;

    @Autowired
    private VirtualThreadService virtualThreadService;

    @Autowired
    private PinDetectionService pinDetectionService;

    @Autowired
    private ScopeValueService scopeValueService;

    @Autowired
    private StructuredConcurrencyService structuredConcurrencyService;

    @Autowired
    private VirtualThreadMetricsService virtualThreadMetricsService;

    @Test
    void testVirtualThreadServiceIntegration() {
        // 测试基础任务执行
        VirtualThreadTaskDto task = virtualThreadService.executeBasicTask("IntegrationTestTask", 100);

        assertNotNull(task);
        assertNotNull(task.getTaskId());
        assertEquals("IntegrationTestTask", task.getTaskName());
        assertEquals(VirtualThreadTaskDto.TaskStatus.COMPLETED, task.getStatus());
        assertTrue(task.getIsVirtualThread());

        // 测试批量任务执行
        List<VirtualThreadTaskDto> tasks = virtualThreadService.executeBatchTasks(10, 50);
        assertNotNull(tasks);
        assertEquals(10, tasks.size());
    }

    @Test
    void testPinDetectionServiceIntegration() {
        PinDetectionReport report = pinDetectionService.detectPinnedThreads();

        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertNotNull(report.getStartTime());
        assertNotNull(report.getEndTime());
        assertNotNull(report.getPinEvents());
        assertEquals(report.getTotalPinEvents(), report.getPinEvents().size());
    }

    @Test
    void testScopeValueServiceIntegration() throws Exception {
        String result = scopeValueService.demonstrateScopedValue();

        assertNotNull(result);
        assertTrue(result.contains("UserId:") || result.contains("user-"));

        List<String> childResults = scopeValueService.executeChildTasks();
        assertNotNull(childResults);
    }

    @Test
    void testStructuredConcurrencyServiceIntegration() {
        // 测试基础结构化并发
        StructuredConcurrencyResult result1 = structuredConcurrencyService.executeBasicStructuredTasks();

        assertNotNull(result1);
        assertNotNull(result1.getResultId());
        assertTrue(result1.getSuccess());
        assertFalse(result1.getTaskResults().isEmpty());

        // 测试 ShutdownOnSuccess
        StructuredConcurrencyResult result2 = structuredConcurrencyService.executeShutdownOnSuccess();

        assertNotNull(result2);
        assertTrue(result2.getSuccess());
        assertEquals(1, result2.getTaskResults().size());

        // 测试错误处理
        StructuredConcurrencyResult result3 = structuredConcurrencyService.demonstrateErrorHandling();

        assertNotNull(result3);
        assertNotNull(result3.getResultId());
    }

    @Test
    void testVirtualThreadMetricsServiceIntegration() {
        PerformanceComparisonReport report = virtualThreadMetricsService.comparePerformance(50, 10);

        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertEquals(50, report.getTaskCount());
        assertNotNull(report.getTraditionalThreadPool());
        assertNotNull(report.getVirtualThreads());

        // 验证性能指标
        assertNotNull(report.getTraditionalThreadPool().getTotalDurationMillis());
        assertNotNull(report.getVirtualThreads().getTotalDurationMillis());
        assertTrue(report.getTraditionalThreadPool().getThroughput() > 0);
        assertTrue(report.getVirtualThreads().getThroughput() > 0);
    }

    @Test
    void testControllerIntegration() {
        // 测试基础任务接口
        var basicTaskResponse = virtualThreadController.executeBasicTask("ControllerTestTask", 100);
        assertEquals(200, basicTaskResponse.getStatusCodeValue());
        assertNotNull(basicTaskResponse.getBody());

        // 测试 Pin 检测接口
        var pinDetectionResponse = virtualThreadController.detectPinnedThreads();
        assertEquals(200, pinDetectionResponse.getStatusCodeValue());
        assertNotNull(pinDetectionResponse.getBody());

        // 测试 ScopedValue 接口
        var scopedValueResponse = virtualThreadController.demonstrateScopedValue();
        assertEquals(200, scopedValueResponse.getStatusCodeValue());
        assertNotNull(scopedValueResponse.getBody());
    }

    @Test
    void testEndToEndWorkflow() {
        // 完整的端到端工作流测试

        // 1. 执行虚拟线程任务
        VirtualThreadTaskDto task = virtualThreadService.executeBasicTask("E2ETask", 100);
        assertTrue(task.getIsVirtualThread());

        // 2. 检测 Pin 事件
        PinDetectionReport pinReport = pinDetectionService.detectPinnedThreads();
        assertNotNull(pinReport.getReportId());

        // 3. 使用 ScopedValue
        String scopeResult = scopeValueService.demonstrateScopedValue();
        assertNotNull(scopeResult);

        // 4. 执行结构化并发
        StructuredConcurrencyResult structuredResult = structuredConcurrencyService.executeBasicStructuredTasks();
        assertTrue(structuredResult.getSuccess());

        // 5. 性能对比
        PerformanceComparisonReport perfReport = virtualThreadMetricsService.comparePerformance(20, 10);
        assertNotNull(perfReport.getReportId());
    }

    @Test
    void testErrorHandlingIntegration() {
        // 测试 Pin 场景
        VirtualThreadTaskDto pinTask = virtualThreadService.simulatePinning("PinTest", 100);
        assertNotNull(pinTask);
        assertTrue(pinTask.getIsVirtualThread());

        // 测试错误处理
        StructuredConcurrencyResult errorResult = structuredConcurrencyService.demonstrateErrorHandling();
        assertNotNull(errorResult);
        assertNotNull(errorResult.getStrategy());
    }
}
