package com.example.demo.circuitbreaker.controller;

import com.example.demo.circuitbreaker.annotation.CallerRateLimiter;
import com.example.demo.circuitbreaker.dto.CircuitBreakerResultDto;
import com.example.demo.circuitbreaker.dto.ExternalApiRequestDto;
import com.example.demo.circuitbreaker.model.CallerRateLimit;
import com.example.demo.circuitbreaker.service.CallerRateLimiterService;
import com.example.demo.circuitbreaker.service.CircuitBreakerMetricsService;
import com.example.demo.circuitbreaker.service.CircuitBreakerService;
import com.example.demo.circuitbreaker.vo.CircuitBreakerStateVo;
import com.example.demo.circuitbreaker.vo.MetricsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 熔断器控制器
 * 提供熔断器、限流器、舱壁隔离、超时控制等容错模式的 API 接口
 */
@RestController
@RequestMapping("/api/circuitbreaker")
@Tag(name = "Circuit Breaker", description = "熔断器和限流器 API")
@Slf4j
public class CircuitBreakerController {

    private final CircuitBreakerService circuitBreakerService;
    private final CallerRateLimiterService callerRateLimiterService;
    private final CircuitBreakerMetricsService metricsService;

    public CircuitBreakerController(
            CircuitBreakerService circuitBreakerService,
            CallerRateLimiterService callerRateLimiterService,
            CircuitBreakerMetricsService metricsService) {
        this.circuitBreakerService = circuitBreakerService;
        this.callerRateLimiterService = callerRateLimiterService;
        this.metricsService = metricsService;
    }

    // ==================== 熔断器相关接口 ====================

    @PostMapping("/circuit-breaker")
    @Operation(summary = "熔断器示例", description = "演示熔断器模式，当失败率超过阈值时熔断器打开")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "503", description = "熔断器已打开，服务暂时不可用")
    public ResponseEntity<CircuitBreakerResultDto> callWithCircuitBreaker(
            @Valid @RequestBody ExternalApiRequestDto request) {
        log.info("Circuit breaker call for endpoint: {}", request.getEndpoint());
        CircuitBreakerResultDto result = circuitBreakerService.callExternalApiWithCircuitBreaker(request);

        if (result.isSuccess()) {
            metricsService.recordSuccess();
            return ResponseEntity.ok(result);
        } else {
            metricsService.recordFailure();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
    }

    // ==================== 限流器相关接口 ====================

    @PostMapping("/rate-limiter")
    @Operation(summary = "限流器示例", description = "演示限流器模式，限制单位时间内的请求数")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "429", description = "超过限流阈值")
    public ResponseEntity<CircuitBreakerResultDto> callWithRateLimiter(
            @Parameter(description = "API端点", example = "/api/users")
            @RequestParam String endpoint) {
        log.info("Rate limiter call for endpoint: {}", endpoint);
        CircuitBreakerResultDto result = circuitBreakerService.callApiWithRateLimit(endpoint);

        if (result.isSuccess()) {
            metricsService.recordSuccess();
            return ResponseEntity.ok(result);
        } else {
            metricsService.recordRejection();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
        }
    }

    // ==================== 舱壁隔离相关接口 ====================

    @PostMapping("/bulkhead")
    @Operation(summary = "舱壁隔离示例", description = "演示舱壁隔离模式，限制并发请求数")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "429", description = "并发请求数超过限制")
    public ResponseEntity<CircuitBreakerResultDto> callWithBulkhead(
            @Parameter(description = "API端点", example = "/api/products")
            @RequestParam String endpoint) {
        log.info("Bulkhead call for endpoint: {}", endpoint);
        CircuitBreakerResultDto result = circuitBreakerService.callApiWithBulkhead(endpoint);

        if (result.isSuccess()) {
            metricsService.recordSuccess();
            return ResponseEntity.ok(result);
        } else {
            metricsService.recordRejection();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
        }
    }

    // ==================== 超时控制相关接口 ====================

    @PostMapping("/time-limiter")
    @Operation(summary = "超时控制示例", description = "演示超时控制模式，请求超时时自动取消")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "504", description = "请求超时")
    public ResponseEntity<CircuitBreakerResultDto> callWithTimeLimiter(
            @Parameter(description = "API端点", example = "/api/orders")
            @RequestParam String endpoint,
            @Parameter(description = "延迟时间（毫秒）", example = "3000")
            @RequestParam(defaultValue = "3000") long delayMs) throws Exception {
        log.info("Time limiter call for endpoint: {} with delay: {}ms", endpoint, delayMs);

        long startTime = System.currentTimeMillis();
        String result = circuitBreakerService.callApiWithTimeout(endpoint, delayMs).get();

        CircuitBreakerResultDto response = CircuitBreakerResultDto.builder()
                .success(true)
                .message(result)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .resiliencePattern("TIMEOUT")
                .build();

        metricsService.recordSuccess();
        return ResponseEntity.ok(response);
    }

    // ==================== 组合容错模式接口 ====================

    @PostMapping("/all-resilience")
    @Operation(summary = "组合所有容错模式", description = "演示同时使用熔断、限流、舱壁隔离的组合模式")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "503", description = "服务暂时不可用或超过限流")
    public ResponseEntity<CircuitBreakerResultDto> callWithAllResiliencePatterns(
            @Valid @RequestBody ExternalApiRequestDto request) {
        log.info("Combined resilience patterns call for endpoint: {}", request.getEndpoint());
        CircuitBreakerResultDto result = circuitBreakerService.callApiWithAllResiliencePatterns(request);

        if (result.isSuccess()) {
            metricsService.recordSuccess();
            return ResponseEntity.ok(result);
        } else {
            metricsService.recordFailure();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
    }

    // ==================== X-Caller 限流接口 ====================

    @GetMapping("/rate-limit/basic")
    @Operation(summary = "X-Caller 基础限流", description = "演示基于 X-Caller Header 的基础限流功能")
    @ApiResponse(responseCode = "200", description = "操作成功",
            headers = @Header(name = "X-Caller", description = "调用方标识，如: mobile, web, admin", required = true))
    @ApiResponse(responseCode = "429", description = "超过限流阈值")
    public ResponseEntity<Map<String, Object>> basicRateLimit(
            @Parameter(description = "数据", example = "test-data")
            @RequestParam(defaultValue = "test-data") String data) {
        log.info("Basic rate limited call for data: {}", data);
        String result = callerRateLimiterService.basicRateLimitedCall(data);
        metricsService.recordSuccess();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/rate-limit/caller-specific")
    @Operation(summary = "X-Caller 差异化限流", description = "演示不同调用方的差异化限流配额")
    @ApiResponse(responseCode = "200", description = "操作成功",
            headers = @Header(name = "X-Caller", description = "调用方标识，如: mobile(100/秒), web(50/秒), admin(1000/秒)", required = true))
    @ApiResponse(responseCode = "429", description = "超过该调用方的限流阈值")
    public ResponseEntity<CircuitBreakerResultDto> callerSpecificRateLimit(
            @Parameter(description = "操作名称", example = "query-users")
            @RequestParam(defaultValue = "test-operation") String operation) {
        log.info("Caller-specific rate limited operation: {}", operation);
        CircuitBreakerResultDto result = callerRateLimiterService.callerSpecificRateLimitedCall(operation);
        metricsService.recordSuccess();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/rate-limit/with-param")
    @Operation(summary = "参数限流", description = "演示从方法参数获取调用方标识的限流")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "429", description = "超过限流阈值")
    public ResponseEntity<CircuitBreakerResultDto> rateLimitWithParam(
            @Parameter(description = "调用方ID，如: premium(100/秒), free(5/秒)", example = "premium")
            @RequestParam String callerId,
            @Parameter(description = "操作名称", example = "process-payment")
            @RequestParam(defaultValue = "process") String operation) {
        log.info("Rate limited call with param - caller: {}, operation: {}", callerId, operation);
        CircuitBreakerResultDto result = callerRateLimiterService.rateLimitedCallWithCallerParam(callerId, operation);
        metricsService.recordSuccess();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/rate-limit/strict")
    @Operation(summary = "严格限流", description = "演示严格限流配置（3请求/秒）")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "429", description = "超过限流阈值")
    public ResponseEntity<Map<String, Object>> strictRateLimit(
            @Parameter(description = "数据", example = "test-data")
            @RequestParam(defaultValue = "test-data") String data) {
        log.info("Strict rate limited call for data: {}", data);
        String result = callerRateLimiterService.strictRateLimitedCall(data);
        metricsService.recordSuccess();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/rate-limit/relaxed")
    @Operation(summary = "宽松限流", description = "演示宽松限流配置（1000请求/秒）")
    @ApiResponse(responseCode = "200", description = "操作成功")
    public ResponseEntity<Map<String, Object>> relaxedRateLimit(
            @Parameter(description = "数据", example = "test-data")
            @RequestParam(defaultValue = "test-data") String data) {
        log.info("Relaxed rate limited call for data: {}", data);
        String result = callerRateLimiterService.relaxedRateLimitedCall(data);
        metricsService.recordSuccess();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "timestamp", LocalDateTime.now()
        ));
    }

    // ==================== 状态查询接口 ====================

    @GetMapping("/state/{name}")
    @Operation(summary = "获取熔断器状态", description = "查询指定熔断器的当前状态")
    public ResponseEntity<CircuitBreakerStateVo> getCircuitBreakerState(
            @Parameter(description = "熔断器名称", example = "externalApi")
            @PathVariable String name) {
        var stateDto = metricsService.getCircuitBreakerState(name);
        CircuitBreakerStateVo stateVo = CircuitBreakerStateVo.fromState(
                name,
                stateDto.getState(),
                stateDto.getFailureRate()
        );
        return ResponseEntity.ok(stateVo);
    }

    @GetMapping("/state/all")
    @Operation(summary = "获取所有熔断器状态", description = "查询所有熔断器的当前状态")
    public ResponseEntity<Map<String, CircuitBreakerStateVo>> getAllCircuitBreakerStates() {
        var stateDtos = metricsService.getAllCircuitBreakerStates();
        Map<String, CircuitBreakerStateVo> result = new HashMap<>();

        stateDtos.forEach((name, stateDto) -> {
            CircuitBreakerStateVo stateVo = CircuitBreakerStateVo.fromState(
                    name,
                    stateDto.getState(),
                    stateDto.getFailureRate()
            );
            result.put(name, stateVo);
        });

        return ResponseEntity.ok(result);
    }

    // ==================== 指标查询接口 ====================

    @GetMapping("/metrics")
    @Operation(summary = "获取指标", description = "查询熔断器和限流器的指标数据")
    public ResponseEntity<Map<String, Object>> getMetrics(
            @Parameter(description = "熔断器名称", example = "externalApi")
            @RequestParam(defaultValue = "externalApi") String circuitBreakerName) {
        var metrics = metricsService.getAllMetrics(circuitBreakerName);
        var bulkheadMetrics = metricsService.getBulkheadMetrics();
        var healthSummary = metricsService.getHealthSummary();
        var overallStats = metricsService.getOverallStats();

        return ResponseEntity.ok(Map.of(
                "circuitBreakerMetrics", metrics,
                "bulkheadMetrics", bulkheadMetrics,
                "healthSummary", healthSummary,
                "overallStats", overallStats,
                "timestamp", LocalDateTime.now()
        ));
    }

    @PostMapping("/metrics/reset")
    @Operation(summary = "重置指标", description = "重置自定义指标计数器")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        metricsService.resetMetrics();
        return ResponseEntity.ok(Map.of(
                "message", "Metrics reset successfully",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/reset/{name}")
    @Operation(summary = "重置熔断器", description = "将指定熔断器重置为关闭状态")
    public ResponseEntity<Map<String, String>> resetCircuitBreaker(
            @Parameter(description = "熔断器名称", example = "externalApi")
            @PathVariable String name) {
        metricsService.resetCircuitBreaker(name);
        return ResponseEntity.ok(Map.of(
                "message", "Circuit breaker reset successfully",
                "name", name,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ==================== 配置查询接口 ====================

    @GetMapping("/config/default-rate-limit")
    @Operation(summary = "获取默认限流配置", description = "查询默认的限流器配置")
    public ResponseEntity<CallerRateLimit> getDefaultRateLimitConfig() {
        return ResponseEntity.ok(circuitBreakerService.getDefaultRateLimitConfig());
    }

    @GetMapping("/config/recommended/{caller}")
    @Operation(summary = "获取推荐限流配置", description = "查询指定调用方的推荐限流配置")
    public ResponseEntity<CallerRateLimit> getRecommendedConfig(
            @Parameter(description = "调用方标识", example = "mobile")
            @PathVariable String caller) {
        CallerRateLimit config = callerRateLimiterService.getRecommendedConfigForCaller(caller);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/circuit-breakers")
    @Operation(summary = "获取所有熔断器名称", description = "列出所有已注册的熔断器")
    public ResponseEntity<Set<String>> getAllCircuitBreakerNames() {
        return ResponseEntity.ok(circuitBreakerService.getAllCircuitBreakerNames());
    }

    // ==================== 综合演示接口 ====================

    /**
     * 综合演示接口 - 演示所有容错模式
     * 使用 X-Caller 限流控制访问频率
     */
    @GetMapping("/demo-all")
    @Operation(summary = "综合演示所有功能", description = "演示所有容错模式和 X-Caller 限流功能",
            responses = {
                    @ApiResponse(responseCode = "200", description = "演示成功",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "429", description = "超过限流阈值")
            })
    @CallerRateLimiter(
            prefix = "demoLimiter",
            defaultLimitForPeriod = 5,
            callerConfigs = "mobile=10,1,5;web=20,1,5;admin=100,1,10"
    )
    public ResponseEntity<Map<String, Object>> demoAll() {
        log.info("Executing comprehensive demo");

        Map<String, Object> demo = new HashMap<>();
        demo.put("title", "Spring Cloud Circuit Breaker (Resilience4j) Demo");
        demo.put("timestamp", LocalDateTime.now());

        // 可用的容错模式
        demo.put("resiliencePatterns", Map.of(
                "circuitBreaker", "熔断器 - 失败率超过阈值时打开",
                "rateLimiter", "限流器 - 限制单位时间内的请求数",
                "bulkhead", "舱壁隔离 - 限制并发请求数",
                "timeout", "超时控制 - 请求超时自动取消",
                "callerRateLimit", "X-Caller 限流 - 基于调用方的差异化限流"
        ));

        // API 端点列表
        demo.put("apiEndpoints", Map.of(
                "circuitBreaker", "POST /api/circuitbreaker/circuit-breaker",
                "rateLimiter", "POST /api/circuitbreaker/rate-limiter",
                "bulkhead", "POST /api/circuitbreaker/bulkhead",
                "timeout", "POST /api/circuitbreaker/time-limiter",
                "allResilience", "POST /api/circuitbreaker/all-resilience",
                "callerRateLimit", "GET /api/circuitbreaker/rate-limit/caller-specific",
                "state", "GET /api/circuitbreaker/state/{name}",
                "metrics", "GET /api/circuitbreaker/metrics"
        ));

        // X-Caller 配置示例
        demo.put("callerRateLimitExample", Map.of(
                "mobile", "100 请求/秒",
                "web", "50 请求/秒",
                "admin", "1000 请求/秒",
                "default", "10 请求/秒",
                "usage", "设置 HTTP Header: X-Caller: mobile"
        ));

        // 当前系统状态
        demo.put("systemStatus", metricsService.getHealthSummary());

        return ResponseEntity.ok(demo);
    }

    // ==================== 测试接口 ====================

    @GetMapping("/simulate-failure")
    @Operation(summary = "模拟失败", description = "模拟API调用失败，用于测试熔断器")
    public ResponseEntity<Map<String, Object>> simulateFailure(
            @Parameter(description = "是否模拟失败", example = "true")
            @RequestParam(defaultValue = "true") boolean simulate) {

        ExternalApiRequestDto request = ExternalApiRequestDto.builder()
                .endpoint("/api/test")
                .simulateFailure(simulate)
                .build();

        CircuitBreakerResultDto result = circuitBreakerService.callExternalApiWithCircuitBreaker(request);

        if (result.isSuccess()) {
            metricsService.recordSuccess();
        } else {
            metricsService.recordFailure();
        }

        return ResponseEntity.ok(Map.of(
                "result", result,
                "instruction", "连续调用此接口多次可触发熔断器打开"
        ));
    }
}
