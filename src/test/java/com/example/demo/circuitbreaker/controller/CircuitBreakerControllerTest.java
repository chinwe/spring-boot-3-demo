package com.example.demo.circuitbreaker.controller;

import com.example.demo.circuitbreaker.dto.CircuitBreakerResultDto;
import com.example.demo.circuitbreaker.dto.ExternalApiRequestDto;
import com.example.demo.circuitbreaker.model.CallerRateLimit;
import com.example.demo.circuitbreaker.service.CallerRateLimiterService;
import com.example.demo.circuitbreaker.service.CircuitBreakerMetricsService;
import com.example.demo.circuitbreaker.service.CircuitBreakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 熔断器控制器集成测试
 */
@WebMvcTest(CircuitBreakerController.class)
class CircuitBreakerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CircuitBreakerService circuitBreakerService;

    @MockBean
    private CallerRateLimiterService callerRateLimiterService;

    @MockBean
    private CircuitBreakerMetricsService metricsService;

    @BeforeEach
    void setUp() {
        // 默认成功的响应
        doNothing().when(metricsService).recordSuccess();
        doNothing().when(metricsService).recordFailure();
        doNothing().when(metricsService).recordRejection();
    }

    // ==================== 熔断器相关测试 ====================

    @Test
    void testCallWithCircuitBreaker_Success() throws Exception {
        // Given
        ExternalApiRequestDto request = ExternalApiRequestDto.builder()
                .endpoint("/api/users")
                .simulateFailure(false)
                .build();

        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(true)
                .message("Success")
                .circuitBreakerName("externalApi")
                .state("CLOSED")
                .resiliencePattern("CIRCUIT_BREAKER")
                .timestamp(LocalDateTime.now())
                .build();

        when(circuitBreakerService.callExternalApiWithCircuitBreaker(any()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/circuit-breaker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resiliencePattern").value("CIRCUIT_BREAKER"));
    }

    @Test
    void testCallWithCircuitBreaker_Failure() throws Exception {
        // Given
        ExternalApiRequestDto request = ExternalApiRequestDto.builder()
                .endpoint("/api/users")
                .simulateFailure(true)
                .build();

        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(false)
                .message("Failure")
                .circuitBreakerName("externalApi")
                .state("OPEN")
                .error("Exception")
                .resiliencePattern("CIRCUIT_BREAKER")
                .timestamp(LocalDateTime.now())
                .build();

        when(circuitBreakerService.callExternalApiWithCircuitBreaker(any()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/circuit-breaker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }

    // ==================== 限流器相关测试 ====================

    @Test
    void testCallWithRateLimiter() throws Exception {
        // Given
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(true)
                .message("Success")
                .resiliencePattern("RATE_LIMITER")
                .timestamp(LocalDateTime.now())
                .build();

        when(circuitBreakerService.callApiWithRateLimit(anyString()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/rate-limiter")
                        .param("endpoint", "/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resiliencePattern").value("RATE_LIMITER"));
    }

    // ==================== 舱壁隔离相关测试 ====================

    @Test
    void testCallWithBulkhead() throws Exception {
        // Given
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(true)
                .message("Success")
                .resiliencePattern("BULKHEAD")
                .timestamp(LocalDateTime.now())
                .build();

        when(circuitBreakerService.callApiWithBulkhead(anyString()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/bulkhead")
                        .param("endpoint", "/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resiliencePattern").value("BULKHEAD"));
    }

    // ==================== 超时控制相关测试 ====================

    @Test
    void testCallWithTimeLimiter() throws Exception {
        // Given
        String expectedResult = "Slow API response from /api/timeout after 1000ms";

        when(circuitBreakerService.callApiWithTimeout(anyString(), eq(1000L)))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/time-limiter")
                        .param("endpoint", "/api/timeout")
                        .param("delayMs", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(expectedResult))
                .andExpect(jsonPath("$.resiliencePattern").value("TIMEOUT"));
    }

    // ==================== X-Caller 限流测试 ====================

    @Test
    void testCallerSpecificRateLimit() throws Exception {
        // Given
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(true)
                .message("Success")
                .resiliencePattern("CALLER_RATE_LIMITER")
                .timestamp(LocalDateTime.now())
                .build();

        when(callerRateLimiterService.callerSpecificRateLimitedCall(anyString()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/rate-limit/caller-specific")
                        .param("operation", "test-operation")
                        .header("X-Caller", "mobile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resiliencePattern").value("CALLER_RATE_LIMITER"));
    }

    @Test
    void testRateLimitWithParam() throws Exception {
        // Given
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(true)
                .message("Success")
                .resiliencePattern("PARAM_RATE_LIMITER")
                .timestamp(LocalDateTime.now())
                .build();

        when(callerRateLimiterService.rateLimitedCallWithCallerParam(anyString(), anyString()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/rate-limit/with-param")
                        .param("callerId", "premium")
                        .param("operation", "process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resiliencePattern").value("PARAM_RATE_LIMITER"));
    }

    // ==================== 状态查询测试 ====================

    @Test
    void testGetCircuitBreakerState() throws Exception {
        // Given
        when(metricsService.getCircuitBreakerState(eq("externalApi")))
                .thenReturn(com.example.demo.circuitbreaker.dto.CircuitBreakerStateDto.builder()
                        .name("externalApi")
                        .state("CLOSED")
                        .failureRate(0.0)
                        .build());

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/state/externalApi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("externalApi"))
                .andExpect(jsonPath("$.state").value("CLOSED"));
    }

    @Test
    void testGetAllCircuitBreakerStates() throws Exception {
        // Given
        Map<String, com.example.demo.circuitbreaker.dto.CircuitBreakerStateDto> states = Map.of(
                "externalApi", com.example.demo.circuitbreaker.dto.CircuitBreakerStateDto.builder()
                        .name("externalApi")
                        .state("CLOSED")
                        .failureRate(0.0)
                        .build()
        );

        when(metricsService.getAllCircuitBreakerStates()).thenReturn(states);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/state/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalApi.state").value("CLOSED"));
    }

    // ==================== 指标查询测试 ====================

    @Test
    void testGetMetrics() throws Exception {
        // Given
        com.example.demo.circuitbreaker.dto.MetricsDto metrics = com.example.demo.circuitbreaker.dto.MetricsDto.builder()
                .circuitBreakerName("externalApi")
                .totalCalls(100)
                .successfulCalls(95)
                .failedCalls(5)
                .failureRate(5.0)
                .build();

        when(metricsService.getAllMetrics(eq("externalApi"))).thenReturn(metrics);
        when(metricsService.getBulkheadMetrics()).thenReturn(Map.of());
        when(metricsService.getHealthSummary()).thenReturn(Map.of());
        when(metricsService.getOverallStats()).thenReturn(Map.of());

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/metrics")
                        .param("circuitBreakerName", "externalApi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.circuitBreakerMetrics.circuitBreakerName").value("externalApi"));
    }

    @Test
    void testResetMetrics() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/metrics/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Metrics reset successfully"));
    }

    @Test
    void testResetCircuitBreaker() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/circuitbreaker/reset/externalApi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Circuit breaker reset successfully"))
                .andExpect(jsonPath("$.name").value("externalApi"));
    }

    // ==================== 配置查询测试 ====================

    @Test
    void testGetDefaultRateLimitConfig() throws Exception {
        // Given
        CallerRateLimit config = CallerRateLimit.defaultConfig();

        when(circuitBreakerService.getDefaultRateLimitConfig()).thenReturn(config);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/config/default-rate-limit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caller").value("default"))
                .andExpect(jsonPath("$.limitForPeriod").value(10));
    }

    @Test
    void testGetRecommendedConfig() throws Exception {
        // Given
        CallerRateLimit config = CallerRateLimit.builder()
                .caller("mobile")
                .limitForPeriod(100)
                .limitRefreshPeriodInSeconds(1)
                .timeoutDurationInSeconds(5)
                .build();

        when(callerRateLimiterService.getRecommendedConfigForCaller(eq("mobile")))
                .thenReturn(config);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/config/recommended/mobile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caller").value("mobile"))
                .andExpect(jsonPath("$.limitForPeriod").value(100));
    }

    @Test
    void testGetAllCircuitBreakerNames() throws Exception {
        // Given
        Set<String> names = Set.of("externalApi", "combinedCircuitBreaker");

        when(circuitBreakerService.getAllCircuitBreakerNames()).thenReturn(names);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/circuit-breakers"))
                .andExpect(status().isOk());
    }

    // ==================== 综合演示测试 ====================

    @Test
    void testDemoAll() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/demo-all")
                        .header("X-Caller", "web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Cloud Circuit Breaker (Resilience4j) Demo"))
                .andExpect(jsonPath("$.resiliencePatterns").exists())
                .andExpect(jsonPath("$.apiEndpoints").exists())
                .andExpect(jsonPath("$.callerRateLimitExample").exists())
                .andExpect(jsonPath("$.systemStatus").exists());
    }

    @Test
    void testSimulateFailure() throws Exception {
        // Given
        CircuitBreakerResultDto result = CircuitBreakerResultDto.builder()
                .success(true)
                .message("Success")
                .circuitBreakerName("externalApi")
                .state("CLOSED")
                .resiliencePattern("CIRCUIT_BREAKER")
                .timestamp(LocalDateTime.now())
                .build();

        when(circuitBreakerService.callExternalApiWithCircuitBreaker(any()))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/circuitbreaker/simulate-failure")
                        .param("simulate", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.instruction").exists());
    }
}
