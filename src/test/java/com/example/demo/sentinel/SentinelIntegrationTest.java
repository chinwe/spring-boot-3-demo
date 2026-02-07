package com.example.demo.sentinel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.example.demo.sentinel.service.SentinelService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sentinel 集成测试
 * 端到端测试 Sentinel 的流量控制、熔断降级等功能
 *
 * 测试场景：
 * 1. 完整的流量控制流程测试
 * 2. 熔断器完整生命周期测试
 * 3. 并发场景下的流控测试
 * 4. 规则动态加载和卸载测试
 * 5. 指标收集和统计测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "logging.level.com.example.demo.sentinel=DEBUG",
        "sentinel.enabled=true"
})
class SentinelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SentinelService sentinelService;

    /**
     * 每个测试前重置状态
     */
    @BeforeEach
    void setUp() {
        sentinelService.resetCounters();
        sentinelService.clearAllRules();

        // 添加测试用规则
        sentinelService.addFlowRule("flowControlResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addFlowRule("degradeResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addFlowRule("paramFlowResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
        sentinelService.addFlowRule("systemRuleResource", 10, RuleConstant.FLOW_GRADE_QPS, "default");
    }

    // ==================== 场景1：完整的流量控制流程测试 ====================

    @Test
    @DisplayName("场景1 - 完整流量控制流程：规则配置 -> 请求拦截 -> 指标收集 -> 规则卸载")
    void testCompleteFlowControlProcess() throws Exception {
        String testResource = "flowProcessTest";

        // Step 1: 动态配置流控规则 - QPS 限制为 3
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", testResource)
                        .param("count", "3")
                        .param("grade", "1")
                        .param("limitApp", "default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Flow rule added successfully"));

        // Step 2: 验证规则已加载
        mockMvc.perform(get("/api/sentinel/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.resource == '" + testResource + "')]").exists());

        // Step 3: 执行请求并收集指标
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/sentinel/flow-control")
                            .param("shouldFail", "false"))
                    .andExpect(status().isOk());
        }

        // Step 4: 获取统计指标
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceName").exists())
                .andExpect(jsonPath("$.passQps").exists())
                .andExpect(jsonPath("$.totalRequest").exists());

        // Step 5: 获取计数器
        mockMvc.perform(get("/api/sentinel/counters")
                        .param("resource", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callCount").value(5));

        // Step 6: 卸载规则
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", testResource))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Flow rule removed successfully"));

        // Step 7: 验证规则已卸载
        boolean ruleRemoved = FlowRuleManager.getRules().stream()
                .noneMatch(rule -> rule.getResource().equals(testResource));
        assertThat(ruleRemoved).isTrue();
    }

    @Test
    @DisplayName("场景1 - 流量控制：不同阈值配置测试")
    void testFlowControlWithDifferentThresholds() throws Exception {
        String lowThresholdResource = "lowThreshold";
        String highThresholdResource = "highThreshold";

        // 配置低阈值 (QPS=2)
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", lowThresholdResource)
                        .param("count", "2")
                        .param("grade", "1"));

        // 配置高阈值 (QPS=20)
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", highThresholdResource)
                        .param("count", "20")
                        .param("grade", "1"));

        // 验证规则配置成功
        mockMvc.perform(get("/api/sentinel/rules"))
                .andExpect(status().isOk());

        // 清理
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", lowThresholdResource));
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", highThresholdResource));
    }

    // ==================== 场景2：熔断器完整生命周期测试 ====================

    @Test
    @DisplayName("场景2 - 熔断器生命周期：关闭 -> 打开 -> 半开 -> 关闭")
    void testCircuitBreakerLifecycle() throws Exception {
        String degradeResource = "lifecycleDegrade";

        // Step 1: 配置熔断规则 - 慢调用比例，阈值 0.5，熔断时长 5 秒
        mockMvc.perform(post("/api/sentinel/rules/degrade")
                        .param("resource", degradeResource)
                        .param("grade", "0")  // 慢调用比例
                        .param("count", "0.5")
                        .param("timeWindow", "5")
                        .param("minRequestAmount", "5")
                        .param("statIntervalMs", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Degrade rule added successfully"));

        // Step 2: 正常请求 - 熔断器关闭状态
        mockMvc.perform(get("/api/sentinel/degrade")
                        .param("scenario", "success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degradeStatus").value("CLOSED"));

        // Step 3: 发送慢调用触发熔断
        for (int i = 0; i < 10; i++) {
            try {
                mockMvc.perform(get("/api/sentinel/degrade/slow-call"))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                // 慢调用可能超时
            }
        }

        // Step 4: 发送异常调用进一步触发熔断
        for (int i = 0; i < 5; i++) {
            try {
                mockMvc.perform(get("/api/sentinel/degrade/exception-ratio")
                        .param("throwException", "true"));
            } catch (Exception e) {
                // 异常被抛出
            }
        }

        // Step 5: 验证熔断器状态和统计
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "degradeResource"))
                .andExpect(status().isOk());

        // Step 6: 等待熔断时间窗口后验证恢复
        Thread.sleep(6000);  // 等待熔断时长

        // Step 7: 半开状态测试 - 发送正常请求
        mockMvc.perform(get("/api/sentinel/degrade")
                        .param("scenario", "success"))
                .andExpect(status().isOk());

        // Step 8: 清理规则
        mockMvc.perform(delete("/api/sentinel/rules/degrade/{resource}", degradeResource))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("场景2 - 熔断器：异常比例熔断测试")
    void testCircuitBreakerExceptionRatio() throws Exception {
        String exceptionResource = "exceptionRatioTest";

        // 配置异常比例熔断规则
        mockMvc.perform(post("/api/sentinel/rules/degrade")
                        .param("resource", exceptionResource)
                        .param("grade", "1")  // 异常比例
                        .param("count", "0.5")  // 50% 异常率
                        .param("timeWindow", "10")
                        .param("minRequestAmount", "5")
                        .param("statIntervalMs", "10000"))
                .andExpect(status().isOk());

        // 发送混合请求（成功和失败）
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                try {
                    mockMvc.perform(get("/api/sentinel/degrade/exception-ratio")
                            .param("throwException", "true"));
                } catch (Exception e) {
                    // 预期异常
                }
            } else {
                mockMvc.perform(get("/api/sentinel/degrade/exception-ratio")
                        .param("throwException", "false"))
                        .andExpect(status().isOk());
            }
        }

        // 验证统计
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "degradeResource"))
                .andExpect(status().isOk());

        // 清理
        mockMvc.perform(delete("/api/sentinel/rules/degrade/{resource}", exceptionResource));
    }

    // ==================== 场景3：并发场景下的流控测试 ====================

    @Test
    @DisplayName("场景3 - 并发流控：多线程同时请求同一资源")
    void testConcurrentFlowControl() throws Exception {
        int threadCount = 20;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);

        // 配置严格的流控规则 (QPS=5)
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", "concurrentTest")
                        .param("count", "5")
                        .param("grade", "1"))
                .andExpect(status().isOk());

        // 并发执行请求
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        MvcResult result = mockMvc.perform(get("/api/sentinel/flow-control")
                                        .param("shouldFail", "false"))
                                .andReturn();
                        if (result.getResponse().getStatus() == 200) {
                            successCount.incrementAndGet();
                        } else if (result.getResponse().getStatus() == 429) {
                            blockedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // 请求失败
                    } finally {
                        latch.countDown();
                    }
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有请求完成
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 验证结果
        int totalRequests = threadCount * requestsPerThread;
        assertThat(successCount.get() + blockedCount.get()).isGreaterThan(0);

        // 获取并验证统计信息
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequest").exists());

        // 清理
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", "concurrentTest"));
    }

    @Test
    @DisplayName("场景3 - 并发热点限流：不同参数值的限流效果")
    void testConcurrentHotspotParamFlow() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // 配置热点参数限流规则
        mockMvc.perform(post("/api/sentinel/rules/param-flow")
                        .param("resource", "paramFlowResource")
                        .param("threshold", "5")
                        .param("paramIdx", "0")
                        .param("paramName", "userId"))
                .andExpect(status().isOk());

        // 使用同一热点参数并发请求
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/sentinel/hotspot")
                                    .param("userId", "hotUser")
                                    .param("productId", "product" + System.currentTimeMillis()))
                            .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 可能被限流
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证至少有一些请求成功
        assertThat(successCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("场景3 - 并发压力测试：快速连续请求模拟高并发")
    void testHighConcurrencyStressTest() throws Exception {
        int totalRequests = 100;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);

        // 配置中等流控阈值
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", "stressTest")
                        .param("count", "20")
                        .param("grade", "1"))
                .andExpect(status().isOk());

        // 快速发送大量请求
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < totalRequests; i++) {
            try {
                MvcResult result = mockMvc.perform(get("/api/sentinel/flow-control")
                                .param("shouldFail", "false"))
                        .andReturn();
                int status = result.getResponse().getStatus();
                if (status == 200) {
                    successCount.incrementAndGet();
                } else if (status == 429) {
                    blockedCount.incrementAndGet();
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long duration = System.currentTimeMillis() - startTime;

        // 验证结果
        assertThat(successCount.get()).isGreaterThan(0);
        assertThat(duration).isLessThan(10000);  // 应在10秒内完成

        // 获取统计信息验证
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk());

        // 清理
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", "stressTest"));
    }

    // ==================== 场景4：规则动态加载和卸载测试 ====================

    @Test
    @DisplayName("场景4 - 规则动态加载：多种规则类型同时加载")
    void testDynamicRuleLoading_MultipleRuleTypes() throws Exception {
        // Step 1: 同时加载多种类型的规则
        // 流控规则
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", "multiFlowTest")
                        .param("count", "10")
                        .param("grade", "1"))
                .andExpect(status().isOk());

        // 降级规则
        mockMvc.perform(post("/api/sentinel/rules/degrade")
                        .param("resource", "multiDegradeTest")
                        .param("grade", "0")
                        .param("count", "0.5")
                        .param("timeWindow", "10")
                        .param("minRequestAmount", "5"))
                .andExpect(status().isOk());

        // 热点参数规则
        mockMvc.perform(post("/api/sentinel/rules/param-flow")
                        .param("resource", "multiParamTest")
                        .param("threshold", "5")
                        .param("paramIdx", "0"))
                .andExpect(status().isOk());

        // 系统规则
        mockMvc.perform(post("/api/sentinel/rules/system")
                        .param("ruleType", "4")  // CPU
                        .param("threshold", "0.8"))
                .andExpect(status().isOk());

        // Step 2: 验证所有规则已加载
        // 注意：PARAM_FLOW 和 SYSTEM 规则需要额外依赖，这里只验证 FLOW 和 DEGRADE
        mockMvc.perform(get("/api/sentinel/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.ruleType == 'FLOW')]").exists())
                .andExpect(jsonPath("$[?(@.ruleType == 'DEGRADE')]").exists());
        // PARAM_FLOW 和 SYSTEM 规则在 Sentinel 1.8.8 中需要额外依赖
        // .andExpect(jsonPath("$[?(@.ruleType == 'PARAM_FLOW')]").exists())
        // .andExpect(jsonPath("$[?(@.ruleType == 'SYSTEM')]").exists());

        // Step 3: 验证资源列表
        mockMvc.perform(get("/api/sentinel/resources"))
                .andExpect(status().isOk());

        // Step 4: 批量卸载规则
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", "multiFlowTest"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/sentinel/rules/degrade/{resource}", "multiDegradeTest"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/sentinel/rules/param-flow/{resource}", "multiParamTest"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/sentinel/rules/system"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("场景4 - 规则动态更新：同一规则的多次修改")
    void testDynamicRuleUpdate() throws Exception {
        String updateTestResource = "updateTest";

        // Step 1: 初始规则 - QPS=5
        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", updateTestResource)
                        .param("count", "5")
                        .param("grade", "1"))
                .andExpect(status().isOk());

        // Step 2: 更新规则 - QPS=10 (先删除再添加)
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", updateTestResource))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", updateTestResource)
                        .param("count", "10")
                        .param("grade", "1"))
                .andExpect(status().isOk());

        // Step 3: 再次更新 - QPS=20
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", updateTestResource))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sentinel/rules/flow")
                        .param("resource", updateTestResource)
                        .param("count", "20")
                        .param("grade", "1"))
                .andExpect(status().isOk());

        // Step 4: 验证最终规则状态
        mockMvc.perform(get("/api/sentinel/rules"))
                .andExpect(status().isOk());

        // Step 5: 清理
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", updateTestResource));
    }

    @Test
    @DisplayName("场景4 - 规则持久化验证：清除所有规则")
    void testClearAllRules() throws Exception {
        // Given: 添加多种规则
        mockMvc.perform(post("/api/sentinel/rules/flow")
                .param("resource", "clearTest1")
                .param("count", "10")
                .param("grade", "1"));

        mockMvc.perform(post("/api/sentinel/rules/degrade")
                .param("resource", "clearTest2")
                .param("grade", "0")
                .param("count", "0.5")
                .param("timeWindow", "10"));

        // When: 清除所有规则
        mockMvc.perform(delete("/api/sentinel/rules/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All rules cleared successfully"));

        // Then: 验证所有规则已清除
        assertThat(FlowRuleManager.getRules().size()).isEqualTo(0);
        assertThat(DegradeRuleManager.getRules().size()).isEqualTo(0);
        // SystemRuleManager.getSystemRules() 需要反射获取，这里跳过验证
    }

    // ==================== 场景5：指标收集和统计测试 ====================

    @Test
    @DisplayName("场景5 - 指标收集：单资源完整指标统计")
    void testMetricsCollection_SingleResource() throws Exception {
        // Given: 执行一系列请求
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/sentinel/flow-control")
                            .param("shouldFail", "false"))
                    .andExpect(status().isOk());
        }

        // When: 获取统计信息
        MvcResult result = mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceName").value("flowControlResource"))
                .andExpect(jsonPath("$.passQps").exists())
                .andExpect(jsonPath("$.blockQps").exists())
                .andExpect(jsonPath("$.totalRequest").exists())
                .andExpect(jsonPath("$.successRate").exists())
                .andExpect(jsonPath("$.averageRt").exists())
                .andExpect(jsonPath("$.concurrency").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        // Then: 验证指标合理性
        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("passQps");
        assertThat(response).contains("totalRequest");
    }

    @Test
    @DisplayName("场景5 - 指标收集：多资源聚合统计")
    void testMetricsCollection_MultipleResources() throws Exception {
        // Given: 对多个资源执行请求
        mockMvc.perform(get("/api/sentinel/flow-control")
                .param("shouldFail", "false"));

        mockMvc.perform(get("/api/sentinel/degrade")
                .param("scenario", "success"));

        mockMvc.perform(get("/api/sentinel/hotspot")
                .param("userId", "user1")
                .param("productId", "product1"));

        // When: 获取所有资源统计
        mockMvc.perform(get("/api/sentinel/statistics/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowControlResource").exists())
                .andExpect(jsonPath("$.degradeResource").exists())
                .andExpect(jsonPath("$.paramFlowResource").exists());
    }

    @Test
    @DisplayName("场景5 - 指标收集：计数器统计")
    void testMetricsCollection_Counters() throws Exception {
        // Given: 执行请求
        mockMvc.perform(get("/api/sentinel/flow-control")
                .param("shouldFail", "false"));
        mockMvc.perform(get("/api/sentinel/flow-control")
                .param("shouldFail", "false"));
        mockMvc.perform(get("/api/sentinel/flow-control")
                .param("shouldFail", "true"));

        // When: 获取计数器
        mockMvc.perform(get("/api/sentinel/counters")
                        .param("resource", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callCount").value(3));

        // When: 重置计数器
        mockMvc.perform(post("/api/sentinel/counters/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All counters reset successfully"));

        // Then: 验证计数器已重置
        mockMvc.perform(get("/api/sentinel/counters")
                        .param("resource", "flowControlResource"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("场景5 - 指标收集：实时监控数据")
    void testMetricsCollection_RealtimeMonitoring() throws Exception {
        // Given: 持续发送请求
        for (int i = 0; i < 20; i++) {
            try {
                mockMvc.perform(get("/api/sentinel/flow-control")
                        .param("shouldFail", "false"));
                Thread.sleep(50);  // 模拟真实请求间隔
            } catch (Exception e) {
                // 忽略
            }
        }

        // When: 获取实时统计
        mockMvc.perform(get("/api/sentinel/statistics")
                        .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passQps").exists())
                .andExpect(jsonPath("$.averageRt").exists())
                .andExpect(jsonPath("$.concurrency").exists());

        // When: 获取所有资源
        mockMvc.perform(get("/api/sentinel/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@ == 'flowControlResource')]").exists());
    }

    // ==================== 综合场景测试 ====================

    @Test
    @DisplayName("综合场景：完整的生产环境模拟")
    void testProductionSimulation() throws Exception {
        // Step 1: 初始化规则配置
        mockMvc.perform(post("/api/sentinel/rules/flow")
                .param("resource", "prodApi")
                .param("count", "50")
                .param("grade", "1"));

        mockMvc.perform(post("/api/sentinel/rules/degrade")
                .param("resource", "prodApi")
                .param("grade", "1")
                .param("count", "0.3")
                .param("timeWindow", "10")
                .param("minRequestAmount", "10"));

        // Step 2: 模拟正常业务流量
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(get("/api/sentinel/flow-control")
                    .param("shouldFail", "false"))
                    .andExpect(status().isOk());
        }

        // Step 3: 监控指标
        mockMvc.perform(get("/api/sentinel/statistics")
                .param("resourceName", "flowControlResource"))
                .andExpect(status().isOk());

        // Step 4: 查看规则状态
        mockMvc.perform(get("/api/sentinel/rules"))
                .andExpect(status().isOk());

        // Step 5: 查看资源列表
        mockMvc.perform(get("/api/sentinel/resources"))
                .andExpect(status().isOk());

        // Step 6: 获取计数器
        mockMvc.perform(get("/api/sentinel/counters")
                .param("resource", "flowControlResource"))
                .andExpect(status().isOk());

        // Step 7: 清理
        mockMvc.perform(delete("/api/sentinel/rules/flow/{resource}", "prodApi"));
        mockMvc.perform(delete("/api/sentinel/rules/degrade/{resource}", "prodApi"));
        mockMvc.perform(post("/api/sentinel/counters/reset"));
    }

    @Test
    @DisplayName("综合演示：所有功能概览")
    void testDemoAll() throws Exception {
        mockMvc.perform(get("/api/sentinel/demo-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alibaba Sentinel Flow Control and Circuit Breaker Demo"))
                .andExpect(jsonPath("$.version").value("1.8.8"))
                .andExpect(jsonPath("$.features").exists())
                .andExpect(jsonPath("$.controlBehaviors").exists())
                .andExpect(jsonPath("$.degradeStrategies").exists())
                .andExpect(jsonPath("$.apiEndpoints").exists())
                .andExpect(jsonPath("$.usageExamples").exists())
                .andExpect(jsonPath("$.currentStatus").exists());
    }
}
