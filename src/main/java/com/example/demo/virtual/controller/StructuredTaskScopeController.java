package com.example.demo.virtual.controller;

import com.example.demo.virtual.dto.OrderAggregationResult;
import com.example.demo.virtual.dto.PaymentRaceResult;
import com.example.demo.virtual.service.StructuredConcurrencyService;
import com.example.demo.virtual.service.StructuredTaskScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * StructuredTaskScope 控制器
 * 提供基于真实 JDK StructuredTaskScope API 的结构化并发演示
 */
@RestController
@RequestMapping("/api/virtual/sts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "StructuredTaskScope", description = "JDK StructuredTaskScope 真实 API 演示")
public class StructuredTaskScopeController {

    private final StructuredTaskScopeService structuredTaskScopeService;
    private final StructuredConcurrencyService structuredConcurrencyService;

    /**
     * ShutdownOnFailure — 订单详情聚合
     */
    @PostMapping("/shutdown-on-failure")
    @Operation(summary = "ShutdownOnFailure 演示", description = "并行获取用户、订单项、支付状态，任一失败即取消全部")
    public ResponseEntity<OrderAggregationResult> shutdownOnFailure(
            @Parameter(description = "订单 ID") @RequestParam Long orderId,
            @Parameter(description = "用户 ID") @RequestParam Long userId) {

        log.info("ShutdownOnFailure: orderId={}, userId={}", orderId, userId);
        OrderAggregationResult result = structuredTaskScopeService.aggregateOrderDetails(orderId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * ShutdownOnSuccess — 支付网关竞速
     */
    @PostMapping("/shutdown-on-success")
    @Operation(summary = "ShutdownOnSuccess 演示", description = "多个支付网关竞速查询，第一个成功即返回")
    public ResponseEntity<PaymentRaceResult> shutdownOnSuccess(
            @Parameter(description = "订单 ID") @RequestParam Long orderId) {

        log.info("ShutdownOnSuccess: orderId={}", orderId);
        PaymentRaceResult result = structuredTaskScopeService.racePaymentStatus(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * 自定义 Scope — 降级聚合
     */
    @PostMapping("/custom-scope")
    @Operation(summary = "自定义 Scope 演示", description = "降级聚合：核心数据源必须成功，非核心失败不影响整体")
    public ResponseEntity<OrderAggregationResult> customScope(
            @Parameter(description = "订单 ID") @RequestParam Long orderId,
            @Parameter(description = "用户 ID") @RequestParam Long userId) {

        log.info("CustomScope degraded: orderId={}, userId={}", orderId, userId);
        OrderAggregationResult result = structuredTaskScopeService.aggregateWithDegradation(orderId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * CompletableFuture vs StructuredTaskScope 对比
     */
    @GetMapping("/compare")
    @Operation(summary = "对比两种实现", description = "对比 CompletableFuture 模拟与真实 StructuredTaskScope 的行为差异")
    public ResponseEntity<Map<String, Object>> compare() {
        log.info("Comparing CompletableFuture vs StructuredTaskScope");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "Comparison not yet implemented - see existing CompletableFuture demos and new STS demos separately");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }

    /**
     * 综合演示
     */
    @GetMapping("/demo-all")
    @Operation(summary = "综合演示所有 StructuredTaskScope 功能", description = "一次性运行所有演示并返回汇总结果")
    public ResponseEntity<Map<String, Object>> demoAll() {
        log.info("=== Running comprehensive StructuredTaskScope demo ===");

        Map<String, Object> demoResults = new LinkedHashMap<>();

        try {
            // 使用默认 ID 进行演示（实际使用时需要数据库中有对应数据）
            demoResults.put("shutdownOnFailure",
                    structuredTaskScopeService.aggregateOrderDetails(1L, 1L));
            demoResults.put("shutdownOnSuccess",
                    structuredTaskScopeService.racePaymentStatus(1L));
            demoResults.put("customScope",
                    structuredTaskScopeService.aggregateWithDegradation(1L, 1L));

            demoResults.put("status", "All demos completed successfully");
            demoResults.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("Demo failed", e);
            demoResults.put("status", "Demo failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(demoResults);
        }

        return ResponseEntity.ok(demoResults);
    }
}
