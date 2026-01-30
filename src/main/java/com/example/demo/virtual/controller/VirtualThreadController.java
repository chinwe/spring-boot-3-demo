package com.example.demo.virtual.controller;

import com.example.demo.virtual.dto.*;
import com.example.demo.virtual.service.*;
import com.example.demo.virtual.vo.PinDetectionVo;
import com.example.demo.virtual.vo.VirtualThreadTaskVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 虚拟线程控制器
 * 提供虚拟线程、Pin 检测、ScopedValue 和结构化并发的 API 接口
 */
@RestController
@RequestMapping("/api/virtual")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Virtual Thread", description = "Java 25 虚拟线程、Pin 检测、ScopedValue 和结构化并发 API")
public class VirtualThreadController {

    private final VirtualThreadService virtualThreadService;
    private final PinDetectionService pinDetectionService;
    private final ScopeValueService scopeValueService;
    private final StructuredConcurrencyService structuredConcurrencyService;
    private final VirtualThreadMetricsService virtualThreadMetricsService;

    /**
     * 执行基础虚拟线程任务
     */
    @GetMapping("/basic-task")
    @Operation(summary = "执行基础虚拟线程任务", description = "演示虚拟线程的基本用法和特性")
    public ResponseEntity<VirtualThreadTaskVo> executeBasicTask(
            @Parameter(description = "任务名称") @RequestParam(defaultValue = "BasicTask") String taskName,
            @Parameter(description = "延迟时间（毫秒）") @RequestParam(defaultValue = "100") int delayMillis) {

        log.info("Executing basic task: {} with delay: {} ms", taskName, delayMillis);
        VirtualThreadTaskDto result = virtualThreadService.executeBasicTask(taskName, delayMillis);
        return ResponseEntity.ok(result.toVo());
    }

    /**
     * 批量执行虚拟线程任务
     */
    @GetMapping("/batch-tasks")
    @Operation(summary = "批量执行虚拟线程任务", description = "展示虚拟线程的高并发能力")
    public ResponseEntity<List<VirtualThreadTaskVo>> executeBatchTasks(
            @Parameter(description = "任务数量") @RequestParam(defaultValue = "100") int taskCount,
            @Parameter(description = "延迟时间（毫秒）") @RequestParam(defaultValue = "50") int delayMillis) {

        log.info("Executing {} batch tasks with delay: {} ms", taskCount, delayMillis);
        List<VirtualThreadTaskDto> results = virtualThreadService.executeBatchTasks(taskCount, delayMillis);

        List<VirtualThreadTaskVo> vos = results.stream()
                .map(VirtualThreadTaskDto::toVo)
                .toList();

        return ResponseEntity.ok(vos);
    }

    /**
     * 检测 Pin 线程事件
     */
    @GetMapping("/pin-detection")
    @Operation(summary = "检测 Pin 线程事件", description = "检测和报告虚拟线程被固定的场景")
    public ResponseEntity<PinDetectionVo> detectPinnedThreads() {
        log.info("Starting pin detection");
        PinDetectionReport report = pinDetectionService.detectPinnedThreads();
        return ResponseEntity.ok(report.toVo());
    }

    /**
     * 测试 Pin 场景
     */
    @PostMapping("/pin-test")
    @Operation(summary = "测试 Pin 场景", description = "执行可能导致 Pin 的操作以检测固定事件")
    public ResponseEntity<Map<String, Object>> testPinScenario(
            @Parameter(description = "Pin 类型") @RequestParam(defaultValue = "SYNCHRONIZED") String pinType) {

        log.info("Testing pin scenario: {}", pinType);

        Map<String, Object> result = Map.of(
                "pinType", pinType,
                "message", "Pin test completed. Check logs for details.",
                "timestamp", System.currentTimeMillis()
        );

        switch (pinType.toUpperCase()) {
            case "SYNCHRONIZED" -> {
                pinDetectionService.testSynchronizedPin(5, 100);
                result = Map.of(
                        "pinType", pinType,
                        "message", "synchronized block test completed. Pin events should be logged.",
                        "timestamp", System.currentTimeMillis()
                );
            }
            case "NATIVE" -> {
                PinDetectionReport.PinEvent event = pinDetectionService.testNativeCodePin();
                result = Map.of(
                        "pinType", pinType,
                        "event", event,
                        "timestamp", System.currentTimeMillis()
                );
            }
            case "FILE_IO" -> {
                List<PinDetectionReport.PinEvent> events = pinDetectionService.testFileIOPin();
                result = Map.of(
                        "pinType", pinType,
                        "events", events,
                        "timestamp", System.currentTimeMillis()
                );
            }
            default -> {
                result = Map.of(
                        "error", "Unknown pin type: " + pinType,
                        "availableTypes", List.of("SYNCHRONIZED", "NATIVE", "FILE_IO")
                );
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 演示 ScopedValue
     */
    @GetMapping("/scoped-value")
    @Operation(summary = "演示 ScopedValue", description = "展示 ScopedValue 的基本用法")
    public ResponseEntity<Map<String, String>> demonstrateScopedValue() {
        log.info("Demonstrating ScopedValue");
        String result = scopeValueService.demonstrateScopedValue();
        return ResponseEntity.ok(Map.of("result", result));
    }

    /**
     * 对比 ThreadLocal 与 ScopedValue
     */
    @GetMapping("/scoped-value-comparison")
    @Operation(summary = "对比 ThreadLocal 与 ScopedValue", description = "对比两种机制的性能和使用方式")
    public ResponseEntity<Map<String, String>> compareThreadLocalVsScopedValue() {
        log.info("Comparing ThreadLocal vs ScopedValue");
        String result = scopeValueService.compareThreadLocalVsScopedValue();
        return ResponseEntity.ok(Map.of("comparison", result));
    }

    /**
     * 执行结构化并发任务
     */
    @PostMapping("/structured-concurrency")
    @Operation(summary = "执行结构化并发任务", description = "使用 StructuredTaskScope 执行并发任务")
    public ResponseEntity<StructuredConcurrencyResult> executeStructuredConcurrency(
            @Parameter(description = "并发策略") @RequestParam(defaultValue = "JOIN_ALL") String strategy) {

        log.info("Executing structured concurrency with strategy: {}", strategy);

        StructuredConcurrencyResult result = switch (strategy.toUpperCase()) {
            case "JOIN_ALL" -> structuredConcurrencyService.executeBasicStructuredTasks();
            case "SHUTDOWN_ON_SUCCESS" -> structuredConcurrencyService.executeShutdownOnSuccess();
            case "ERROR_HANDLING" -> structuredConcurrencyService.demonstrateErrorHandling();
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        };

        return ResponseEntity.ok(result);
    }

    /**
     * 演示 ShutdownOnSuccess
     */
    @GetMapping("/structured-concurrency/shutdown-on-success")
    @Operation(summary = "演示 ShutdownOnSuccess", description = "展示任一任务成功即关闭的模式")
    public ResponseEntity<StructuredConcurrencyResult> shutdownOnSuccess() {
        log.info("Executing ShutdownOnSuccess demo");
        StructuredConcurrencyResult result = structuredConcurrencyService.executeShutdownOnSuccess();
        return ResponseEntity.ok(result);
    }

    /**
     * 演示错误处理
     */
    @GetMapping("/structured-concurrency/error-handling")
    @Operation(summary = "演示错误处理", description = "展示结构化并发中的错误处理")
    public ResponseEntity<StructuredConcurrencyResult> errorHandling() {
        log.info("Executing error handling demo");
        StructuredConcurrencyResult result = structuredConcurrencyService.demonstrateErrorHandling();
        return ResponseEntity.ok(result);
    }

    /**
     * 性能对比测试
     */
    @GetMapping("/performance-comparison")
    @Operation(summary = "性能对比测试", description = "对比传统线程池和虚拟线程的性能")
    public ResponseEntity<PerformanceComparisonReport> performanceComparison(
            @Parameter(description = "任务数量") @RequestParam(defaultValue = "1000") int taskCount,
            @Parameter(description = "延迟时间（毫秒）") @RequestParam(defaultValue = "10") int delayMillis) {

        log.info("Running performance comparison with {} tasks, {} ms delay", taskCount, delayMillis);
        PerformanceComparisonReport report = virtualThreadMetricsService.comparePerformance(taskCount, delayMillis);
        return ResponseEntity.ok(report);
    }

    /**
     * 综合演示接口
     */
    @GetMapping("/demo-all")
    @Operation(summary = "综合演示接口", description = "执行所有演示并返回汇总结果")
    public ResponseEntity<Map<String, Object>> demoAll() {
        log.info("=== Running comprehensive virtual thread demo ===");

        Map<String, Object> demoResults = new java.util.HashMap<>();

        try {
            // 1. 基础任务
            demoResults.put("basicTask", virtualThreadService.executeBasicTask("DemoTask", 100).toVo());

            // 2. Pin 检测
            demoResults.put("pinDetection", pinDetectionService.detectPinnedThreads().toVo());

            // 3. ScopedValue
            demoResults.put("scopedValue", scopeValueService.demonstrateScopedValue());

            // 4. 结构化并发
            demoResults.put("structuredConcurrency", structuredConcurrencyService.executeBasicStructuredTasks());

            // 5. 性能对比（小规模）
            demoResults.put("performanceComparison", virtualThreadMetricsService.comparePerformance(100, 10));

            demoResults.put("status", "All demos completed successfully");
            demoResults.put("timestamp", System.currentTimeMillis());

            log.info("=== Comprehensive demo completed ===");

        } catch (Exception e) {
            log.error("Demo failed", e);
            demoResults.put("status", "Demo failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(demoResults);
        }

        return ResponseEntity.ok(demoResults);
    }

    /**
     * 模拟 Pin 场景
     */
    @GetMapping("/simulate-pinning")
    @Operation(summary = "模拟 Pin 场景", description = "演示使用 synchronized 导致的 Pin")
    public ResponseEntity<VirtualThreadTaskVo> simulatePinning(
            @Parameter(description = "任务名称") @RequestParam(defaultValue = "PinTask") String taskName,
            @Parameter(description = "延迟时间（毫秒）") @RequestParam(defaultValue = "200") int delayMillis) {

        log.info("Simulating pinning scenario: {} with delay: {} ms", taskName, delayMillis);
        VirtualThreadTaskDto result = virtualThreadService.simulatePinning(taskName, delayMillis);
        return ResponseEntity.ok(result.toVo());
    }
}
