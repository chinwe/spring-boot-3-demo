package com.example.demo.sentinel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.example.demo.sentinel.dto.RuleInfoDto;
import com.example.demo.sentinel.dto.SentinelMetricsDto;
import com.example.demo.sentinel.dto.SentinelResultDto;
import com.example.demo.sentinel.service.SentinelService;

/**
 * Sentinel 控制器单元测试
 * 测试 Sentinel 控制器的 HTTP 接口
 */
@WebMvcTest(SentinelController.class)
class SentinelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SentinelService sentinelService;

    /**
     * 每个测试前重置 Mock
     */
    @BeforeEach
    void setUp() {
        doNothing().when(sentinelService).resetCounters();
    }

    // ==================== 流量控制接口测试 ====================

    @Test
    @DisplayName("流量控制演示 - 成功场景")
    void testFlowControlDemo_Success() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("flowControlResource")
                .message("Flow control request succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("FLOW_CONTROL")
                .build();
        when(sentinelService.flowControlDemo(false)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/flow-control")
                        .param("shouldFail", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resourceName").value("flowControlResource"))
                .andExpect(jsonPath("$.ruleType").value("FLOW_CONTROL"));

        verify(sentinelService, times(1)).flowControlDemo(false);
    }

    @Test
    @DisplayName("流量控制演示 - 失败场景")
    void testFlowControlDemo_Failure() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(false)
                .resourceName("flowControlResource")
                .message("Fallback response due to exception")
                .error("RuntimeException")
                .errorMessage("Simulated business failure")
                .timestamp(LocalDateTime.now())
                .ruleType("FLOW_CONTROL")
                .build();
        when(sentinelService.flowControlDemo(true)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/flow-control")
                        .param("shouldFail", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("RuntimeException"));

        verify(sentinelService, times(1)).flowControlDemo(true);
    }

    @Test
    @DisplayName("流量控制带排队等待")
    void testFlowControlWithWait() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("flowControlResource")
                .message("Flow control request succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("FLOW_CONTROL")
                .build();
        when(sentinelService.flowControlDemo(false)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/flow-control/with-wait")
                        .param("shouldFail", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sentinelService, times(1)).flowControlDemo(false);
    }

    @Test
    @DisplayName("手动 Entry 流量控制")
    void testManualEntryDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("manualResource")
                .message("Manual entry request succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("MANUAL_ENTRY")
                .build();
        when(sentinelService.manualEntryDemo("manualResource", 1)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/flow-control/manual")
                        .param("resourceName", "manualResource")
                        .param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceName").value("manualResource"))
                .andExpect(jsonPath("$.ruleType").value("MANUAL_ENTRY"));

        verify(sentinelService, times(1)).manualEntryDemo("manualResource", 1);
    }

    // ==================== 熔断降级接口测试 ====================

    @Test
    @DisplayName("熔断降级演示 - 成功场景")
    void testDegradeDemo_Success() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("degradeResource")
                .message("Request succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("DEGRADE")
                .degradeStatus("CLOSED")
                .build();
        when(sentinelService.degradeDemo("success")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/degrade")
                        .param("scenario", "success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.degradeStatus").value("CLOSED"));

        verify(sentinelService, times(1)).degradeDemo("success");
    }

    @Test
    @DisplayName("熔断降级演示 - 慢调用场景")
    void testSlowCallDegradeDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("degradeResource")
                .message("Slow request completed")
                .callCount(1)
                .executionTimeMs(500)
                .timestamp(LocalDateTime.now())
                .ruleType("DEGRADE")
                .degradeStatus("CLOSED")
                .build();
        when(sentinelService.degradeDemo("slow")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/degrade/slow-call"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionTimeMs").value(500));

        verify(sentinelService, times(1)).degradeDemo("slow");
    }

    @Test
    @DisplayName("熔断降级演示 - 异常比例场景")
    void testExceptionRatioDegradeDemo_NoException() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("degradeResource")
                .message("Request succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("DEGRADE")
                .build();
        when(sentinelService.degradeDemo("success")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/degrade/exception-ratio")
                        .param("throwException", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sentinelService, times(1)).degradeDemo("success");
    }

    // ==================== 热点参数限流接口测试 ====================

    @Test
    @DisplayName("热点参数限流演示")
    void testParamFlowDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("paramFlowResource")
                .message("Hot parameter request succeeded")
                .callCount(1)
                .parameters(Map.of("userId", "user123", "productId", "product456"))
                .timestamp(LocalDateTime.now())
                .ruleType("PARAM_FLOW")
                .build();
        when(sentinelService.paramFlowDemo("user123", "product456")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/hotspot")
                        .param("userId", "user123")
                        .param("productId", "product456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleType").value("PARAM_FLOW"))
                .andExpect(jsonPath("$.parameters.userId").value("user123"));

        verify(sentinelService, times(1)).paramFlowDemo("user123", "product456");
    }

    @Test
    @DisplayName("频繁用户限流演示")
    void testFrequentUserFlowControl() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("paramFlowResource")
                .message("Hot parameter request succeeded")
                .callCount(1)
                .parameters(Map.of("userId", "userVIP", "productId", "product-123"))
                .timestamp(LocalDateTime.now())
                .ruleType("PARAM_FLOW")
                .build();
        when(sentinelService.paramFlowDemo(eq("userVIP"), anyString())).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/hotspot/frequent-user")
                        .param("userId", "userVIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parameters.userId").value("userVIP"));

        verify(sentinelService, times(1)).paramFlowDemo(eq("userVIP"), anyString());
    }

    // ==================== 系统自适应保护接口测试 ====================

    @Test
    @DisplayName("系统 CPU 保护演示")
    void testSystemCpuDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("systemRuleResource")
                .message("CPU intensive task completed")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("SYSTEM_RULE")
                .systemLoadType("CPU")
                .build();
        when(sentinelService.systemRuleDemo("cpu")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/system/cpu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemLoadType").value("CPU"));

        verify(sentinelService, times(1)).systemRuleDemo("cpu");
    }

    @Test
    @DisplayName("系统 RT 保护演示")
    void testSystemRtDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("systemRuleResource")
                .message("Long RT task completed")
                .callCount(1)
                .executionTimeMs(100)
                .timestamp(LocalDateTime.now())
                .ruleType("SYSTEM_RULE")
                .systemLoadType("RT")
                .build();
        when(sentinelService.systemRuleDemo("rt")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/system/rt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemLoadType").value("RT"));

        verify(sentinelService, times(1)).systemRuleDemo("rt");
    }

    @Test
    @DisplayName("系统并发保护演示")
    void testSystemConcurrencyDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("systemRuleResource")
                .message("Concurrent task completed")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("SYSTEM_RULE")
                .systemLoadType("CONCURRENCY")
                .build();
        when(sentinelService.systemRuleDemo("concurrency")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/system/concurrency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemLoadType").value("CONCURRENCY"));

        verify(sentinelService, times(1)).systemRuleDemo("concurrency");
    }

    @Test
    @DisplayName("系统 QPS 保护演示")
    void testSystemQpsDemo() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("systemRuleResource")
                .message("Normal task completed")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("SYSTEM_RULE")
                .build();
        when(sentinelService.systemRuleDemo("qps")).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/system/qps"))
                .andExpect(status().isOk());

        verify(sentinelService, times(1)).systemRuleDemo("qps");
    }

    // ==================== 异常追踪接口测试 ====================

    @Test
    @DisplayName("异常追踪演示 - 不追踪")
    void testExceptionTraceDemo_NoTrace() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("exceptionTraceResource")
                .message("Exception trace demo succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("EXCEPTION_TRACE")
                .build();
        when(sentinelService.exceptionTraceDemo(false)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/sentinel/exception-trace")
                        .param("shouldTrace", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleType").value("EXCEPTION_TRACE"));

        verify(sentinelService, times(1)).exceptionTraceDemo(false);
    }

    // ==================== 统计信息接口测试 ====================

    @Test
    @DisplayName("获取实时统计信息")
    void testGetStatistics() throws Exception {
        // Given
        SentinelMetricsDto metrics = SentinelMetricsDto.builder()
                .resourceName("flowControlResource")
                .passQps(10)
                .blockQps(0)
                .totalRequest(100)
                .exceptionQps(0)
                .successRate("100.00%")
                .averageRt(50)
                .concurrency(5)
                .timestamp(LocalDateTime.now())
                .build();
        when(sentinelService.getResourceMetrics("flowControlResource")).thenReturn(metrics);

        // When & Then
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceName").value("flowControlResource"))
                .andExpect(jsonPath("$.passQps").value(10))
                .andExpect(jsonPath("$.successRate").value("100.00%"));

        verify(sentinelService, times(1)).getResourceMetrics("flowControlResource");
    }

    @Test
    @DisplayName("获取所有资源的统计信息")
    void testGetAllStatistics() throws Exception {
        // Given
        Set<String> resources = Set.of("flowControlResource", "degradeResource");
        SentinelMetricsDto metrics1 = SentinelMetricsDto.builder()
                .resourceName("flowControlResource")
                .timestamp(LocalDateTime.now())
                .build();
        SentinelMetricsDto metrics2 = SentinelMetricsDto.builder()
                .resourceName("degradeResource")
                .timestamp(LocalDateTime.now())
                .build();

        when(sentinelService.getAllResourceNames()).thenReturn(resources);
        when(sentinelService.getResourceMetrics("flowControlResource")).thenReturn(metrics1);
        when(sentinelService.getResourceMetrics("degradeResource")).thenReturn(metrics2);

        // When & Then
        mockMvc.perform(get("/api/sentinel/statistics/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowControlResource").exists())
                .andExpect(jsonPath("$.degradeResource").exists());

        verify(sentinelService, times(1)).getAllResourceNames();
    }

    @Test
    @DisplayName("获取计数器值")
    void testGetCounters() throws Exception {
        // Given
        Map<String, Long> counters = Map.of("callCount", 10L, "failureCount", 2L);
        when(sentinelService.getCounters("flowControlResource")).thenReturn(counters);

        // When & Then
        mockMvc.perform(get("/api/sentinel/counters")
                        .param("resource", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callCount").value(10))
                .andExpect(jsonPath("$.failureCount").value(2));

        verify(sentinelService, times(1)).getCounters("flowControlResource");
    }

    @Test
    @DisplayName("重置计数器")
    void testResetCounters() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sentinel/counters/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All counters reset successfully"));

        verify(sentinelService, times(1)).resetCounters();
    }

    // ==================== 规则管理接口测试 ====================

    @Test
    @DisplayName("获取所有规则")
    void testGetAllRules() throws Exception {
        // Given
        List<RuleInfoDto> rules = List.of(
                RuleInfoDto.builder()
                        .resource("flowControlResource")
                        .ruleType("FLOW")
                        .grade("QPS")
                        .count("10")
                        .build(),
                RuleInfoDto.builder()
                        .resource("degradeResource")
                        .ruleType("DEGRADE")
                        .grade("SLOW_CALL_RATIO")
                        .count("0.5")
                        .build()
        );
        when(sentinelService.getAllRules()).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/sentinel/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ruleType").value("FLOW"))
                .andExpect(jsonPath("$[1].ruleType").value("DEGRADE"));

        verify(sentinelService, times(1)).getAllRules();
    }

    @Test
    @DisplayName("添加流量控制规则")
    void testAddFlowRule() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", "testResource")
                        .param("count", "10")
                        .param("grade", "1")
                        .param("limitApp", "default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Flow rule added successfully"))
                .andExpect(jsonPath("$.resource").value("testResource"))
                .andExpect(jsonPath("$.grade").value("QPS"));

        verify(sentinelService, times(1)).addFlowRule("testResource", 10, 1, "default");
    }

    @Test
    @DisplayName("添加熔断降级规则")
    void testAddDegradeRule() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sentinel/rules/degrade")
                        .param("resource", "testDegradeResource")
                        .param("grade", "0")
                        .param("count", "0.5")
                        .param("timeWindow", "10")
                        .param("minRequestAmount", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Degrade rule added successfully"))
                .andExpect(jsonPath("$.resource").value("testDegradeResource"));

        verify(sentinelService, times(1)).addDegradeRule("testDegradeResource", 0, 0.5, 10, 5, 1000);
    }

    @Test
    @DisplayName("添加热点参数流控规则")
    void testAddParamFlowRule() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sentinel/rules/param-flow")
                        .param("resource", "testParamResource")
                        .param("threshold", "10")
                        .param("paramIdx", "0")
                        .param("paramName", "userId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Param flow rule added successfully"))
                .andExpect(jsonPath("$.resource").value("testParamResource"));

        verify(sentinelService, times(1)).addParamFlowRule("testParamResource", 10, 0, "userId");
    }

    @Test
    @DisplayName("添加系统规则")
    void testAddSystemRule() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sentinel/rules/system")
                        .param("ruleType", "4")
                        .param("threshold", "0.8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("System rule added successfully"))
                .andExpect(jsonPath("$.ruleType").value("4"));

        verify(sentinelService, times(1)).addSystemRule(4, 0.8);
    }

    @Test
    @DisplayName("删除流量控制规则")
    void testRemoveFlowRule() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", "testResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Flow rule removed successfully"))
                .andExpect(jsonPath("$.resource").value("testResource"));

        verify(sentinelService, times(1)).removeFlowRule("testResource");
    }

    @Test
    @DisplayName("删除熔断降级规则")
    void testRemoveDegradeRule() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sentinel/rules/degrade/{resource}", "testDegradeResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Degrade rule removed successfully"));

        verify(sentinelService, times(1)).removeDegradeRule("testDegradeResource");
    }

    @Test
    @DisplayName("删除热点参数流控规则")
    void testRemoveParamFlowRule() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sentinel/rules/param-flow/{resource}", "testParamResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Param flow rule removed successfully"));

        verify(sentinelService, times(1)).removeParamFlowRule("testParamResource");
    }

    @Test
    @DisplayName("清除所有系统规则")
    void testClearSystemRules() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sentinel/rules/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("System rules cleared successfully"));

        verify(sentinelService, times(1)).clearSystemRules();
    }

    @Test
    @DisplayName("清除所有规则")
    void testClearAllRules() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sentinel/rules/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All rules cleared successfully"));

        verify(sentinelService, times(1)).clearAllRules();
    }

    @Test
    @DisplayName("获取所有资源名称")
    void testGetAllResources() throws Exception {
        // Given
        Set<String> resources = Set.of("flowControlResource", "degradeResource", "paramFlowResource");
        when(sentinelService.getAllResourceNames()).thenReturn(resources);

        // When & Then
        mockMvc.perform(get("/api/sentinel/resources"))
                .andExpect(status().isOk());

        verify(sentinelService, times(1)).getAllResourceNames();
    }

    // ==================== 综合演示接口测试 ====================

    @Test
    @DisplayName("综合演示所有功能")
    void testDemoAll() throws Exception {
        // Given
        Set<String> resources = Set.of("flowControlResource", "degradeResource");
        List<RuleInfoDto> rules = List.of();
        when(sentinelService.getAllResourceNames()).thenReturn(resources);
        when(sentinelService.getAllRules()).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/sentinel/demo-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alibaba Sentinel Flow Control and Circuit Breaker Demo"))
                .andExpect(jsonPath("$.version").value("1.8.8"))
                .andExpect(jsonPath("$.features").exists())
                .andExpect(jsonPath("$.apiEndpoints").exists())
                .andExpect(jsonPath("$.usageExamples").exists());

        verify(sentinelService, times(1)).getAllResourceNames();
        verify(sentinelService, times(1)).getAllRules();
    }

    // ==================== 参数验证测试 ====================

    @Test
    @DisplayName("默认参数测试")
    void testDefaultParameters() throws Exception {
        // Given
        SentinelResultDto result = SentinelResultDto.builder()
                .success(true)
                .resourceName("flowControlResource")
                .message("Flow control request succeeded")
                .callCount(1)
                .timestamp(LocalDateTime.now())
                .ruleType("FLOW_CONTROL")
                .build();
        when(sentinelService.flowControlDemo(false)).thenReturn(result);

        // When & Then - 不传参数，使用默认值
        mockMvc.perform(get("/api/sentinel/flow-control"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sentinelService, times(1)).flowControlDemo(false);
    }
}
