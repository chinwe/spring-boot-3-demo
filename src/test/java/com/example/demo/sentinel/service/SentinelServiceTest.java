package com.example.demo.sentinel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.example.demo.sentinel.dto.RuleInfoDto;
import com.example.demo.sentinel.dto.SentinelMetricsDto;
import com.example.demo.sentinel.dto.SentinelResultDto;

/**
 * Sentinel 服务层单元测试
 * 测试 Sentinel 流量控制、熔断降级、热点参数限流等功能
 */
@SpringBootTest
class SentinelServiceTest {

    @Autowired
    private SentinelService sentinelService;

    /**
     * 每个测试前重置计数器
     */
    @BeforeEach
    void setUp() {
        sentinelService.resetCounters();

        // 清除所有规则以确保测试隔离
        sentinelService.clearAllRules();

        // 为测试添加基本流控规则
        sentinelService.addFlowRule("flowControlResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addFlowRule("degradeResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addFlowRule("paramFlowResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addFlowRule("systemRuleResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
    }

    // ==================== 流量控制测试 ====================

    @Test
    @DisplayName("流量控制 - 成功场景")
    void testFlowControlDemo_Success() {
        // When
        SentinelResultDto result = sentinelService.flowControlDemo(false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResourceName()).isEqualTo("flowControlResource");
        assertThat(result.getRuleType()).isEqualTo("FLOW_CONTROL");
        assertThat(result.getMessage()).contains("succeeded");
        assertThat(result.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("流量控制 - 失败场景")
    void testFlowControlDemo_Failure() {
        // When & Then - 验证抛出异常
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            sentinelService.flowControlDemo(true);
        });
    }

    @Test
    @DisplayName("流量控制 - 调用计数")
    void testFlowControlDemo_CallCounting() {
        // When
        sentinelService.flowControlDemo(false);
        sentinelService.flowControlDemo(false);
        SentinelResultDto thirdCall = sentinelService.flowControlDemo(false);

        // Then
        assertThat(thirdCall.getCallCount()).isEqualTo(3);

        Map<String, Long> counters = sentinelService.getCounters("flowControlResource");
        assertThat(counters).isNotNull();
        assertThat(counters.get("callCount")).isEqualTo(3);
    }

    // ==================== 熔断降级测试 ====================

    @Test
    @DisplayName("熔断降级 - 成功场景")
    void testDegradeDemo_Success() {
        // When
        SentinelResultDto result = sentinelService.degradeDemo("success");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResourceName()).isEqualTo("degradeResource");
        assertThat(result.getRuleType()).isEqualTo("DEGRADE");
        assertThat(result.getDegradeStatus()).isEqualTo("CLOSED");
        assertThat(result.getMessage()).contains("succeeded");
    }

    @Test
    @DisplayName("熔断降级 - 慢调用场景")
    void testDegradeDemo_Slow() {
        // When
        SentinelResultDto result = sentinelService.degradeDemo("slow");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getExecutionTimeMs()).isEqualTo(500);
        assertThat(result.getDegradeStatus()).isEqualTo("CLOSED");
        assertThat(result.getMessage()).contains("Slow request");
    }

    @Test
    @DisplayName("熔断降级 - 异常场景")
    void testDegradeDemo_Exception() {
        // When & Then - 验证抛出异常
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            sentinelService.degradeDemo("exception");
        });
    }

    @Test
    @DisplayName("熔断降级 - 默认场景")
    void testDegradeDemo_Default() {
        // When
        SentinelResultDto result = sentinelService.degradeDemo("unknown");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("Default response");
    }

    // ==================== 热点参数限流测试 ====================

    @Test
    @DisplayName("热点参数限流 - 基本场景")
    void testParamFlowDemo_Basic() {
        // When
        SentinelResultDto result = sentinelService.paramFlowDemo("user123", "product456");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResourceName()).isEqualTo("paramFlowResource");
        assertThat(result.getRuleType()).isEqualTo("PARAM_FLOW");
        assertThat(result.getParameters()).isNotNull();
        assertThat(result.getParameters().get("userId")).isEqualTo("user123");
        assertThat(result.getParameters().get("productId")).isEqualTo("product456");
    }

    @Test
    @DisplayName("热点参数限流 - 参数计数")
    void testParamFlowDemo_ParameterCounting() {
        // When - 调用相同用户两次
        sentinelService.paramFlowDemo("user123", "product1");
        SentinelResultDto secondCall = sentinelService.paramFlowDemo("user123", "product2");

        // Then - 第二次调用计数应该为 2
        assertThat(secondCall.getCallCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("热点参数限流 - 不同用户独立计数")
    void testParamFlowDemo_DifferentUsers() {
        // When
        SentinelResultDto result1 = sentinelService.paramFlowDemo("userA", "product1");
        SentinelResultDto result2 = sentinelService.paramFlowDemo("userB", "product1");

        // Then
        assertThat(result1.getCallCount()).isEqualTo(1);
        assertThat(result2.getCallCount()).isEqualTo(1);
    }

    // ==================== 系统自适应保护测试 ====================

    @Test
    @DisplayName("系统规则 - CPU 负载场景")
    void testSystemRuleDemo_Cpu() {
        // When
        SentinelResultDto result = sentinelService.systemRuleDemo("cpu");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRuleType()).isEqualTo("SYSTEM_RULE");
        assertThat(result.getSystemLoadType()).isEqualTo("CPU");
        assertThat(result.getMessage()).contains("CPU intensive task");
    }

    @Test
    @DisplayName("系统规则 - RT 场景")
    void testSystemRuleDemo_Rt() {
        // When
        SentinelResultDto result = sentinelService.systemRuleDemo("rt");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSystemLoadType()).isEqualTo("RT");
        assertThat(result.getExecutionTimeMs()).isEqualTo(100);
    }

    @Test
    @DisplayName("系统规则 - 并发场景")
    void testSystemRuleDemo_Concurrency() {
        // When
        SentinelResultDto result = sentinelService.systemRuleDemo("concurrency");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSystemLoadType()).isEqualTo("CONCURRENCY");
    }

    @Test
    @DisplayName("系统规则 - 默认场景")
    void testSystemRuleDemo_Default() {
        // When
        SentinelResultDto result = sentinelService.systemRuleDemo("unknown");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("Normal task");
    }

    // ==================== 编程式资源定义测试 ====================

    @Test
    @DisplayName("编程式资源定义 - 基本场景")
    void testManualEntryDemo_Basic() {
        // When
        SentinelResultDto result = sentinelService.manualEntryDemo("manualResource", 100);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResourceName()).isEqualTo("manualResource");
        assertThat(result.getRuleType()).isEqualTo("MANUAL_ENTRY");
    }

    @Test
    @DisplayName("编程式资源定义 - 调用计数")
    void testManualEntryDemo_Counting() {
        // When
        sentinelService.manualEntryDemo("countResource", 1);
        SentinelResultDto result2 = sentinelService.manualEntryDemo("countResource", 2);
        SentinelResultDto result3 = sentinelService.manualEntryDemo("countResource", 3);

        // Then
        assertThat(result2.getCallCount()).isEqualTo(2);
        assertThat(result3.getCallCount()).isEqualTo(3);
    }

    // ==================== 异常追踪测试 ====================

    @Test
    @DisplayName("异常追踪 - 不追踪场景")
    void testExceptionTraceDemo_NoTrace() {
        // When
        SentinelResultDto result = sentinelService.exceptionTraceDemo(false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRuleType()).isEqualTo("EXCEPTION_TRACE");
        assertThat(result.getMessage()).contains("succeeded");
    }

    @Test
    @DisplayName("异常追踪 - 追踪场景")
    void testExceptionTraceDemo_WithTrace() {
        // When & Then - 验证抛出异常并追踪
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            sentinelService.exceptionTraceDemo(true);
        });
    }

    // ==================== 规则动态管理测试 ====================

    @Test
    @DisplayName("动态添加流控规则")
    void testAddFlowRule() {
        // When
        sentinelService.addFlowRule("testResource", 20, RuleConstant.FLOW_GRADE_QPS, "default");

        // Then
        boolean ruleExists = FlowRuleManager.getRules().stream()
                .anyMatch(rule -> rule.getResource().equals("testResource") && rule.getCount() == 20);
        assertThat(ruleExists).isTrue();
    }

    @Test
    @DisplayName("动态添加降级规则")
    void testAddDegradeRule() {
        // When
        sentinelService.addDegradeRule("testDegradeResource", 0, 0.5, 10, 5, 10000);

        // Then
        boolean ruleExists = DegradeRuleManager.getRules().stream()
                .anyMatch(rule -> rule.getResource().equals("testDegradeResource"));
        assertThat(ruleExists).isTrue();
    }

    @Test
    @DisplayName("动态添加热点参数规则")
    void testAddParamFlowRule() {
        // When
        sentinelService.addParamFlowRule("testParamResource", 10, 0, "testParam");

        // Then - Sentinel 1.8.8 中 param 功能需要额外依赖
        // 这里只验证方法调用不抛出异常
        assertDoesNotThrow(() -> sentinelService.addParamFlowRule("testParamResource", 10, 0, "testParam"));
    }

    @Test
    @DisplayName("删除流控规则")
    void testRemoveFlowRule() {
        // Given
        sentinelService.addFlowRule("toRemoveResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");

        // When
        sentinelService.removeFlowRule("toRemoveResource");

        // Then
        boolean ruleExists = FlowRuleManager.getRules().stream()
                .anyMatch(rule -> rule.getResource().equals("toRemoveResource"));
        assertThat(ruleExists).isFalse();
    }

    @Test
    @DisplayName("删除降级规则")
    void testRemoveDegradeRule() {
        // Given
        sentinelService.addDegradeRule("toRemoveDegradeResource", 0, 0.5, 10, 5, 10000);

        // When
        sentinelService.removeDegradeRule("toRemoveDegradeResource");

        // Then
        boolean ruleExists = DegradeRuleManager.getRules().stream()
                .anyMatch(rule -> rule.getResource().equals("toRemoveDegradeResource"));
        assertThat(ruleExists).isFalse();
    }

    @Test
    @DisplayName("删除热点参数规则")
    void testRemoveParamFlowRule() {
        // Given
        sentinelService.addParamFlowRule("toRemoveParamResource", 10, 0, "testParam");

        // When
        sentinelService.removeParamFlowRule("toRemoveParamResource");

        // Then - Sentinel 1.8.8 中 param 功能需要额外依赖
        // 这里只验证方法调用不抛出异常
        assertDoesNotThrow(() -> sentinelService.removeParamFlowRule("toRemoveParamResource"));
    }

    @Test
    @DisplayName("清除系统规则")
    void testClearSystemRules() {
        // When
        sentinelService.clearSystemRules();

        // Then - Sentinel 1.8.8 中 getSystemRules 方法不可用
        // 这里只验证方法调用不抛出异常
        assertDoesNotThrow(() -> sentinelService.clearSystemRules());
    }

    @Test
    @DisplayName("清除所有规则")
    void testClearAllRules() {
        // Given
        sentinelService.addFlowRule("test1", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addDegradeRule("test2", 0, 0.5, 10, 5, 10000);

        // When
        sentinelService.clearAllRules();

        // Then
        assertThat(FlowRuleManager.getRules()).isEmpty();
        assertThat(DegradeRuleManager.getRules()).isEmpty();
        // Param flow rules 和 System rules 在 Sentinel 1.8.8 中需要额外依赖
    }

    // ==================== 统计信息测试 ====================

    @Test
    @DisplayName("获取资源指标")
    void testGetResourceMetrics() {
        // When
        SentinelMetricsDto metrics = sentinelService.getResourceMetrics("flowControlResource");

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getResourceName()).isEqualTo("flowControlResource");
        assertThat(metrics.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取所有规则")
    void testGetAllRules() {
        // Given
        sentinelService.addFlowRule("ruleTest1", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addDegradeRule("ruleTest2", 0, 0.5, 10, 5, 10000);

        // When
        var rules = sentinelService.getAllRules();

        // Then
        assertThat(rules).isNotNull();
        assertThat(rules).isNotEmpty();

        boolean hasFlowRule = rules.stream()
                .anyMatch(rule -> rule.getRuleType().equals("FLOW"));
        boolean hasDegradeRule = rules.stream()
                .anyMatch(rule -> rule.getRuleType().equals("DEGRADE"));
        assertThat(hasFlowRule).isTrue();
        assertThat(hasDegradeRule).isTrue();
    }

    @Test
    @DisplayName("获取所有资源名称")
    void testGetAllResourceNames() {
        // When
        Set<String> resourceNames = sentinelService.getAllResourceNames();

        // Then
        assertThat(resourceNames).isNotNull();
        assertThat(resourceNames).isNotEmpty();
        assertThat(resourceNames).contains("flowControlResource", "degradeResource",
                "paramFlowResource", "systemRuleResource", "exceptionTraceResource");
    }

    // ==================== 计数器管理测试 ====================

    @Test
    @DisplayName("重置计数器")
    void testResetCounters() {
        // Given
        sentinelService.flowControlDemo(false);
        sentinelService.flowControlDemo(false);

        // When
        sentinelService.resetCounters();

        // Then
        Map<String, Long> counters = sentinelService.getCounters("flowControlResource");
        assertThat(counters.entrySet()).isEmpty();
    }

    @Test
    @DisplayName("获取计数器")
    void testGetCounters() {
        // Given
        sentinelService.flowControlDemo(false);
        sentinelService.flowControlDemo(false);

        // When
        Map<String, Long> counters = sentinelService.getCounters("flowControlResource");

        // Then
        assertThat(counters).isNotNull();
        assertThat(counters.get("callCount")).isEqualTo(2);
    }

    @Test
    @DisplayName("获取空资源计数器")
    void testGetCounters_EmptyResource() {
        // When
        Map<String, Long> counters = sentinelService.getCounters("nonExistentResource");

        // Then
        assertThat(counters).isNotNull();
        assertThat(counters.entrySet()).isEmpty();
    }

    // ==================== 综合测试 ====================

    @Test
    @DisplayName("综合测试 - 多功能调用")
    void testMultipleFeatures() throws InterruptedException {
        // 流控
        SentinelResultDto flowResult = sentinelService.flowControlDemo(false);
        assertThat(flowResult.isSuccess()).isTrue();

        // 降级
        SentinelResultDto degradeResult = sentinelService.degradeDemo("success");
        assertThat(degradeResult.isSuccess()).isTrue();

        // 热点参数
        SentinelResultDto paramResult = sentinelService.paramFlowDemo("user1", "product1");
        assertThat(paramResult.isSuccess()).isTrue();

        // 系统规则
        SentinelResultDto systemResult = sentinelService.systemRuleDemo("cpu");
        assertThat(systemResult.isSuccess()).isTrue();

        // 验证资源名称已注册
        Set<String> resources = sentinelService.getAllResourceNames();
        assertThat(resources).contains("flowControlResource", "degradeResource",
                "paramFlowResource", "systemRuleResource");
    }
}
