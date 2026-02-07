package com.example.demo.sentinel.controller;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.example.demo.sentinel.dto.RuleInfoDto;
import com.example.demo.sentinel.dto.SentinelMetricsDto;
import com.example.demo.sentinel.dto.SentinelResultDto;
import com.example.demo.sentinel.service.SentinelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sentinel 控制器
 * 提供 Sentinel 流量控制、熔断降级、热点参数限流等功能的 API 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/sentinel")
@Tag(name = "Sentinel", description = "Sentinel 流量控制和熔断降级 API")
public class SentinelController {

    @Resource
    private SentinelService sentinelService;

    // ==================== 流量控制接口 ====================

    @GetMapping("/flow-control")
    @Operation(summary = "流量控制演示", description = "演示基于 QPS 的流量控制功能")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "429", description = "被流量控制规则拦截")
    public ResponseEntity<SentinelResultDto> flowControlDemo(
            @Parameter(description = "是否模拟业务失败", example = "false")
            @RequestParam(defaultValue = "false") boolean shouldFail) {
        log.info("Flow control demo request - shouldFail: {}", shouldFail);
        SentinelResultDto result = sentinelService.flowControlDemo(shouldFail);
        return getResponseEntity(result);
    }

    @GetMapping("/flow-control/with-wait")
    @Operation(summary = "流量控制带排队等待", description = "演示使用 Warm Up 流控效果的流量控制")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "429", description = "被流量控制规则拦截")
    public ResponseEntity<SentinelResultDto> flowControlWithWait(
            @Parameter(description = "是否模拟业务失败", example = "false")
            @RequestParam(defaultValue = "false") boolean shouldFail) {
        log.info("Flow control with warm up request - shouldFail: {}", shouldFail);
        SentinelResultDto result = sentinelService.flowControlDemo(shouldFail);
        return getResponseEntity(result);
    }

    @GetMapping("/flow-control/manual")
    @Operation(summary = "手动 Entry 流量控制", description = "演示手动使用 SphU.entry() 进行流量控制")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "429", description = "被流量控制规则拦截")
    public ResponseEntity<SentinelResultDto> manualEntryDemo(
            @Parameter(description = "资源名称", example = "manualResource")
            @RequestParam(defaultValue = "manualResource") String resourceName,
            @Parameter(description = "请求数量", example = "1")
            @RequestParam(defaultValue = "1") int count) {
        log.info("Manual entry demo - resource: {}, count: {}", resourceName, count);
        SentinelResultDto result = sentinelService.manualEntryDemo(resourceName, count);
        return getResponseEntity(result);
    }

    // ==================== 熔断降级接口 ====================

    @GetMapping("/degrade")
    @Operation(summary = "熔断降级演示", description = "演示熔断器的状态转换：关闭 -> 打开 -> 半开 -> 关闭")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被熔断器拦截（服务降级）")
    public ResponseEntity<SentinelResultDto> degradeDemo(
            @Parameter(description = "场景类型: success-成功, slow-慢调用, exception-异常", example = "success")
            @RequestParam(defaultValue = "success") String scenario) {
        log.info("Degrade demo request - scenario: {}", scenario);
        SentinelResultDto result = sentinelService.degradeDemo(scenario);
        return getResponseEntity(result);
    }

    @GetMapping("/degrade/slow-call")
    @Operation(summary = "慢调用熔断演示", description = "演示基于慢调用比例的熔断降级")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被熔断器拦截")
    public ResponseEntity<SentinelResultDto> slowCallDegradeDemo() {
        log.info("Slow call degrade demo request");
        SentinelResultDto result = sentinelService.degradeDemo("slow");
        return getResponseEntity(result);
    }

    @GetMapping("/degrade/exception-ratio")
    @Operation(summary = "异常比例熔断演示", description = "演示基于异常比例的熔断降级")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被熔断器拦截")
    public ResponseEntity<SentinelResultDto> exceptionRatioDegradeDemo(
            @Parameter(description = "是否抛出异常", example = "true")
            @RequestParam(defaultValue = "true") boolean throwException) {
        log.info("Exception ratio degrade demo request - throwException: {}", throwException);
        SentinelResultDto result = sentinelService.degradeDemo(throwException ? "exception" : "success");
        return getResponseEntity(result);
    }

    // ==================== 热点参数限流接口 ====================

    @GetMapping("/hotspot")
    @Operation(summary = "热点参数限流演示", description = "演示基于参数（如 userId）的热点流控")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "429", description = "被热点参数流控规则拦截")
    public ResponseEntity<SentinelResultDto> paramFlowDemo(
            @Parameter(description = "用户ID（热点参数）", example = "user123")
            @RequestParam(defaultValue = "user123") String userId,
            @Parameter(description = "商品ID", example = "product456")
            @RequestParam(defaultValue = "product456") String productId) {
        log.info("Param flow demo request - userId: {}, productId: {}", userId, productId);
        SentinelResultDto result = sentinelService.paramFlowDemo(userId, productId);
        return getResponseEntity(result);
    }

    @GetMapping("/hotspot/frequent-user")
    @Operation(summary = "频繁用户限流演示", description = "演示对特定用户 ID 的热点流控")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "429", description = "热点用户被限流")
    public ResponseEntity<SentinelResultDto> frequentUserFlowControl(
            @Parameter(description = "用户ID（频繁用户如 userVIP 会被限流）", example = "userVIP")
            @RequestParam(defaultValue = "userVIP") String userId) {
        log.info("Frequent user flow control request - userId: {}", userId);
        SentinelResultDto result = sentinelService.paramFlowDemo(userId, "product-" + System.currentTimeMillis());
        return getResponseEntity(result);
    }

    // ==================== 系统自适应保护接口 ====================

    @GetMapping("/system/cpu")
    @Operation(summary = "系统 CPU 保护演示", description = "演示基于系统 CPU 使用率的自适应保护")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被系统规则拦截")
    public ResponseEntity<SentinelResultDto> systemCpuDemo() {
        log.info("System CPU protection demo request");
        SentinelResultDto result = sentinelService.systemRuleDemo("cpu");
        return getResponseEntity(result);
    }

    @GetMapping("/system/rt")
    @Operation(summary = "系统 RT 保护演示", description = "演示基于平均响应时间的自适应保护")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被系统规则拦截")
    public ResponseEntity<SentinelResultDto> systemRtDemo() {
        log.info("System RT protection demo request");
        SentinelResultDto result = sentinelService.systemRuleDemo("rt");
        return getResponseEntity(result);
    }

    @GetMapping("/system/concurrency")
    @Operation(summary = "系统并发保护演示", description = "演示基于并发线程数的自适应保护")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被系统规则拦截")
    public ResponseEntity<SentinelResultDto> systemConcurrencyDemo() {
        log.info("System concurrency protection demo request");
        SentinelResultDto result = sentinelService.systemRuleDemo("concurrency");
        return getResponseEntity(result);
    }

    @GetMapping("/system/qps")
    @Operation(summary = "系统入口 QPS 保护演示", description = "演示基于系统入口 QPS 的自适应保护")
    @ApiResponse(responseCode = "200", description = "请求成功")
    @ApiResponse(responseCode = "503", description = "被系统规则拦截")
    public ResponseEntity<SentinelResultDto> systemQpsDemo() {
        log.info("System QPS protection demo request");
        SentinelResultDto result = sentinelService.systemRuleDemo("qps");
        return getResponseEntity(result);
    }

    // ==================== 异常追踪接口 ====================

    @GetMapping("/exception-trace")
    @Operation(summary = "异常追踪演示", description = "演示使用 Tracer.trace() 追踪异常")
    @ApiResponse(responseCode = "200", description = "请求成功")
    public ResponseEntity<SentinelResultDto> exceptionTraceDemo(
            @Parameter(description = "是否追踪异常", example = "false")
            @RequestParam(defaultValue = "false") boolean shouldTrace) {
        log.info("Exception trace demo request - shouldTrace: {}", shouldTrace);
        try {
            SentinelResultDto result = sentinelService.exceptionTraceDemo(shouldTrace);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SentinelResultDto.builder()
                            .success(false)
                            .resourceName("exceptionTraceResource")
                            .message("Exception occurred and was traced")
                            .error(e.getClass().getSimpleName())
                            .errorMessage(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // ==================== 统计信息接口 ====================

    @GetMapping("/statistics")
    @Operation(summary = "获取实时统计信息", description = "查询指定资源的实时 QPS、拒绝数、成功率等指标")
    public ResponseEntity<SentinelMetricsDto> getStatistics(
            @Parameter(description = "资源名称", example = "flowControlResource")
            @RequestParam(defaultValue = "flowControlResource") String resourceName) {
        log.info("Get statistics for resource: {}", resourceName);
        SentinelMetricsDto metrics = sentinelService.getResourceMetrics(resourceName);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/statistics/all")
    @Operation(summary = "获取所有资源的统计信息", description = "查询所有已定义资源的实时指标")
    public ResponseEntity<Map<String, SentinelMetricsDto>> getAllStatistics() {
        log.info("Get statistics for all resources");
        Map<String, SentinelMetricsDto> allMetrics = new HashMap<>();

        Set<String> resourceNames = sentinelService.getAllResourceNames();
        for (String resourceName : resourceNames) {
            try {
                SentinelMetricsDto metrics = sentinelService.getResourceMetrics(resourceName);
                allMetrics.put(resourceName, metrics);
            } catch (Exception e) {
                log.warn("Failed to get metrics for resource: {}", resourceName, e);
            }
        }

        return ResponseEntity.ok(allMetrics);
    }

    @GetMapping("/counters")
    @Operation(summary = "获取计数器值", description = "查询指定资源的调用和失败计数")
    public ResponseEntity<Map<String, Long>> getCounters(
            @Parameter(description = "资源名称", example = "flowControlResource")
            @RequestParam(defaultValue = "flowControlResource") String resource) {
        log.info("Get counters for resource: {}", resource);
        Map<String, Long> counters = sentinelService.getCounters(resource);
        return ResponseEntity.ok(counters);
    }

    @PostMapping("/counters/reset")
    @Operation(summary = "重置计数器", description = "重置所有调用和失败计数器")
    public ResponseEntity<Map<String, String>> resetCounters() {
        log.info("Reset all counters");
        sentinelService.resetCounters();
        return ResponseEntity.ok(Map.of(
                "message", "All counters reset successfully",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ==================== 规则管理接口 ====================

    @GetMapping("/rules")
    @Operation(summary = "获取所有规则", description = "查询当前加载的所有 Sentinel 规则")
    public ResponseEntity<List<RuleInfoDto>> getAllRules() {
        log.info("Get all Sentinel rules");
        List<RuleInfoDto> rules = sentinelService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/rules/flow")
    @Operation(summary = "添加流量控制规则", description = "动态添加流量控制规则")
    public ResponseEntity<Map<String, String>> addFlowRule(
            @Parameter(description = "资源名称", example = "flowControlResource")
            @RequestParam String resource,
            @Parameter(description = "阈值", example = "5")
            @RequestParam int count,
            @Parameter(description = "阈值类型: 0-线程数, 1-QPS", example = "1")
            @RequestParam(defaultValue = "1") int grade,
            @Parameter(description = "限流应用", example = "default")
            @RequestParam(defaultValue = "default") String limitApp) {
        log.info("Add flow rule - resource: {}, count: {}, grade: {}", resource, count, grade);
        sentinelService.addFlowRule(resource, count, grade, limitApp);
        return ResponseEntity.ok(Map.of(
                "message", "Flow rule added successfully",
                "resource", resource,
                "count", String.valueOf(count),
                "grade", grade == 0 ? "THREAD_COUNT" : "QPS",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/rules/degrade")
    @Operation(summary = "添加熔断降级规则", description = "动态添加熔断降级规则")
    public ResponseEntity<Map<String, String>> addDegradeRule(
            @Parameter(description = "资源名称", example = "degradeResource")
            @RequestParam String resource,
            @Parameter(description = "熔断策略: 0-慢调用比例, 1-异常比例, 2-异常数", example = "0")
            @RequestParam int grade,
            @Parameter(description = "阈值", example = "0.5")
            @RequestParam double count,
            @Parameter(description = "熔断时长（秒）", example = "10")
            @RequestParam int timeWindow,
            @Parameter(description = "最小请求数", example = "5")
            @RequestParam(defaultValue = "5") int minRequestAmount,
            @Parameter(description = "统计时长（毫秒）", example = "1000")
            @RequestParam(defaultValue = "1000") int statIntervalMs) {
        log.info("Add degrade rule - resource: {}, grade: {}, count: {}", resource, grade, count);
        sentinelService.addDegradeRule(resource, grade, count, timeWindow, minRequestAmount, statIntervalMs);
        return ResponseEntity.ok(Map.of(
                "message", "Degrade rule added successfully",
                "resource", resource,
                "grade", String.valueOf(grade),
                "count", String.valueOf(count),
                "timeWindow", String.valueOf(timeWindow),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/rules/param-flow")
    @Operation(summary = "添加热点参数流控规则", description = "动态添加热点参数流控规则")
    public ResponseEntity<Map<String, String>> addParamFlowRule(
            @Parameter(description = "资源名称", example = "paramFlowResource")
            @RequestParam String resource,
            @Parameter(description = "阈值", example = "10")
            @RequestParam int threshold,
            @Parameter(description = "参数索引", example = "0")
            @RequestParam(defaultValue = "0") int paramIdx,
            @Parameter(description = "参数名称", example = "userId")
            @RequestParam(defaultValue = "userId") String paramName) {
        log.info("Add param flow rule - resource: {}, threshold: {}, paramIdx: {}", resource, threshold, paramIdx);
        sentinelService.addParamFlowRule(resource, threshold, paramIdx, paramName);
        return ResponseEntity.ok(Map.of(
                "message", "Param flow rule added successfully",
                "resource", resource,
                "threshold", String.valueOf(threshold),
                "paramIdx", String.valueOf(paramIdx),
                "paramName", paramName,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/rules/system")
    @Operation(summary = "添加系统规则", description = "动态添加系统自适应保护规则")
    public ResponseEntity<Map<String, String>> addSystemRule(
            @Parameter(description = "规则类型: 0-LOAD, 1-RT, 2-THREAD, 3-QPS, 4-CPU", example = "4")
            @RequestParam int ruleType,
            @Parameter(description = "阈值", example = "0.8")
            @RequestParam double threshold) {
        log.info("Add system rule - ruleType: {}, threshold: {}", ruleType, threshold);
        sentinelService.addSystemRule(ruleType, threshold);
        return ResponseEntity.ok(Map.of(
                "message", "System rule added successfully",
                "ruleType", String.valueOf(ruleType),
                "threshold", String.valueOf(threshold),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @DeleteMapping("/rules/flow/{resource}")
    @Operation(summary = "删除流量控制规则", description = "删除指定资源的流量控制规则")
    public ResponseEntity<Map<String, String>> removeFlowRule(
            @Parameter(description = "资源名称", example = "flowControlResource")
            @PathVariable String resource) {
        log.info("Remove flow rule for resource: {}", resource);
        sentinelService.removeFlowRule(resource);
        return ResponseEntity.ok(Map.of(
                "message", "Flow rule removed successfully",
                "resource", resource,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @DeleteMapping("/rules/degrade/{resource}")
    @Operation(summary = "删除熔断降级规则", description = "删除指定资源的熔断降级规则")
    public ResponseEntity<Map<String, String>> removeDegradeRule(
            @Parameter(description = "资源名称", example = "degradeResource")
            @PathVariable String resource) {
        log.info("Remove degrade rule for resource: {}", resource);
        sentinelService.removeDegradeRule(resource);
        return ResponseEntity.ok(Map.of(
                "message", "Degrade rule removed successfully",
                "resource", resource,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @DeleteMapping("/rules/param-flow/{resource}")
    @Operation(summary = "删除热点参数流控规则", description = "删除指定资源的热点参数流控规则")
    public ResponseEntity<Map<String, String>> removeParamFlowRule(
            @Parameter(description = "资源名称", example = "paramFlowResource")
            @PathVariable String resource) {
        log.info("Remove param flow rule for resource: {}", resource);
        sentinelService.removeParamFlowRule(resource);
        return ResponseEntity.ok(Map.of(
                "message", "Param flow rule removed successfully",
                "resource", resource,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @DeleteMapping("/rules/system")
    @Operation(summary = "清除所有系统规则", description = "清除所有系统自适应保护规则")
    public ResponseEntity<Map<String, String>> clearSystemRules() {
        log.info("Clear all system rules");
        sentinelService.clearSystemRules();
        return ResponseEntity.ok(Map.of(
                "message", "System rules cleared successfully",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @DeleteMapping("/rules/all")
    @Operation(summary = "清除所有规则", description = "清除所有 Sentinel 规则")
    public ResponseEntity<Map<String, String>> clearAllRules() {
        log.info("Clear all Sentinel rules");
        sentinelService.clearAllRules();
        return ResponseEntity.ok(Map.of(
                "message", "All rules cleared successfully",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/resources")
    @Operation(summary = "获取所有资源名称", description = "列出所有已定义的资源")
    public ResponseEntity<Set<String>> getAllResources() {
        log.info("Get all resource names");
        Set<String> resources = sentinelService.getAllResourceNames();
        return ResponseEntity.ok(resources);
    }

    // ==================== 综合演示接口 ====================

    @GetMapping("/demo-all")
    @Operation(summary = "综合演示所有功能", description = "演示 Sentinel 的所有流量控制和熔断降级功能",
            responses = {
                    @ApiResponse(responseCode = "200", description = "演示成功",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "429", description = "被限流"),
                    @ApiResponse(responseCode = "503", description = "被熔断")
            })
    public ResponseEntity<Map<String, Object>> demoAll() {
        log.info("Executing comprehensive Sentinel demo");

        Map<String, Object> demo = new HashMap<>();
        demo.put("title", "Alibaba Sentinel Flow Control and Circuit Breaker Demo");
        demo.put("version", "1.8.8");
        demo.put("timestamp", LocalDateTime.now());

        // Sentinel 功能特性
        demo.put("features", Map.of(
                "flowControl", "流量控制 - QPS/线程数限流，支持 Warm Up、排队等待",
                "circuitBreaker", "熔断降级 - 慢调用比例、异常比例、异常数熔断",
                "paramFlowControl", "热点参数限流 - 基于参数值的精细化流控",
                "systemProtection", "系统自适应保护 - CPU、RT、并发、QPS 保护",
                "adaptiveProtection", "自适应保护 - 根据系统负载自动调整"
        ));

        // 流控效果
        demo.put("controlBehaviors", Map.of(
                "default", "快速失败 - 直接拒绝",
                "warmUp", "预热 - 冷启动缓慢增加阈值",
                "throttling", "排队等待 - 匀速排队通过",
                "warmUpThrottling", "预热 + 排队等待"
        ));

        // 熔断策略
        demo.put("degradeStrategies", Map.of(
                "slowCallRatio", "慢调用比例 - 响应时间超过阈值时熔断",
                "exceptionRatio", "异常比例 - 异常率超过阈值时熔断",
                "exceptionCount", "异常数 - 异常数超过阈值时熔断"
        ));

        // API 端点列表
        Map<String, String> apiEndpoints = new LinkedHashMap<>();
        apiEndpoints.put("flowControl", "GET /api/sentinel/flow-control");
        apiEndpoints.put("flowControlWithWait", "GET /api/sentinel/flow-control/with-wait");
        apiEndpoints.put("manualEntry", "GET /api/sentinel/flow-control/manual");
        apiEndpoints.put("degrade", "GET /api/sentinel/degrade");
        apiEndpoints.put("slowCallDegrade", "GET /api/sentinel/degrade/slow-call");
        apiEndpoints.put("exceptionRatioDegrade", "GET /api/sentinel/degrade/exception-ratio");
        apiEndpoints.put("paramFlow", "GET /api/sentinel/hotspot");
        apiEndpoints.put("frequentUserFlow", "GET /api/sentinel/hotspot/frequent-user");
        apiEndpoints.put("systemCpu", "GET /api/sentinel/system/cpu");
        apiEndpoints.put("systemRt", "GET /api/sentinel/system/rt");
        apiEndpoints.put("systemConcurrency", "GET /api/sentinel/system/concurrency");
        apiEndpoints.put("systemQps", "GET /api/sentinel/system/qps");
        apiEndpoints.put("exceptionTrace", "GET /api/sentinel/exception-trace");
        apiEndpoints.put("statistics", "GET /api/sentinel/statistics");
        apiEndpoints.put("allStatistics", "GET /api/sentinel/statistics/all");
        apiEndpoints.put("allRules", "GET /api/sentinel/rules");
        apiEndpoints.put("addFlowRule", "POST /api/sentinel/rules/flow");
        apiEndpoints.put("addDegradeRule", "POST /api/sentinel/rules/degrade");
        apiEndpoints.put("addParamFlowRule", "POST /api/sentinel/rules/param-flow");
        apiEndpoints.put("addSystemRule", "POST /api/sentinel/rules/system");
        apiEndpoints.put("removeFlowRule", "DELETE /api/sentinel/rules/flow/{resource}");
        apiEndpoints.put("removeDegradeRule", "DELETE /api/sentinel/rules/degrade/{resource}");
        apiEndpoints.put("removeParamFlowRule", "DELETE /api/sentinel/rules/param-flow/{resource}");
        apiEndpoints.put("clearSystemRules", "DELETE /api/sentinel/rules/system");
        apiEndpoints.put("clearAllRules", "DELETE /api/sentinel/rules/all");
        apiEndpoints.put("getAllResources", "GET /api/sentinel/resources");
        demo.put("apiEndpoints", apiEndpoints);

        // 当前统计信息
        Map<String, Object> currentStats = new HashMap<>();
        try {
            currentStats.put("resources", sentinelService.getAllResourceNames());
            currentStats.put("rules", sentinelService.getAllRules());
        } catch (Exception e) {
            currentStats.put("error", e.getMessage());
        }
        demo.put("currentStatus", currentStats);

        // 使用示例
        demo.put("usageExamples", Map.of(
                "basicFlowControl", "curl 'http://localhost:8080/api/sentinel/flow-control?shouldFail=false'",
                "degradeTest", "curl 'http://localhost:8080/api/sentinel/degrade?scenario=exception' (多次调用触发熔断)",
                "paramFlowTest", "curl 'http://localhost:8080/api/sentinel/hotspot?userId=userVIP&productId=product123'",
                "addFlowRule", "curl -X POST 'http://localhost:8080/api/sentinel/rules/flow?resource=testResource&count=10&grade=1'",
                "getStatistics", "curl 'http://localhost:8080/api/sentinel/statistics?resource=flowControlResource'"
        ));

        return ResponseEntity.ok(demo);
    }

    // ==================== 工具方法 ====================

    /**
     * 根据结果返回相应的 HTTP 响应
     */
    private ResponseEntity<SentinelResultDto> getResponseEntity(SentinelResultDto result) {
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else if (result.getBlockException() != null) {
            // 被流控或熔断规则拦截
            if ("DEGRADE".equals(result.getRuleType()) || "SYSTEM_RULE".equals(result.getRuleType())) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
            }
        } else {
            // 业务失败
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
