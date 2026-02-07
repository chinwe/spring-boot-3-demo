package com.example.demo.sentinel.service;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.SphU;
import com.example.demo.sentinel.dto.SentinelMetricsDto;
import com.example.demo.sentinel.dto.SentinelResultDto;
import com.example.demo.sentinel.dto.RuleInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Sentinel 服务类
 * 提供 Sentinel 流量控制、熔断降级、热点参数限流等功能演示
 */
@Slf4j
@Service
public class SentinelService {

    // 用于模拟不同场景的计数器
    private final Map<String, AtomicLong> callCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failureCounters = new ConcurrentHashMap<>();

    /**
     * 流量控制演示 - 使用 @SentinelResource 注解
     * 演示基本的 QPS 流量控制
     */
    @SentinelResource(
            value = "flowControlResource",
            blockHandler = "handleFlowControlBlock",
            fallback = "handleFlowControlFallback"
    )
    public SentinelResultDto flowControlDemo(boolean shouldFail) {
        String resourceName = "flowControlResource";
        long callCount = callCounters.computeIfAbsent(resourceName, k -> new AtomicLong(0)).incrementAndGet();

        log.info("Flow control demo - call count: {}", callCount);

        if (shouldFail) {
            long failureCount = failureCounters.computeIfAbsent(resourceName, k -> new AtomicLong(0)).incrementAndGet();
            throw new RuntimeException("Simulated business failure - failure count: " + failureCount);
        }

        return SentinelResultDto.builder()
                .success(true)
                .resourceName(resourceName)
                .message("Flow control request succeeded - call count: " + callCount)
                .callCount(callCount)
                .timestamp(LocalDateTime.now())
                .ruleType("FLOW_CONTROL")
                .build();
    }

    /**
     * 流量控制 Block Handler
     */
    public SentinelResultDto handleFlowControlBlock(boolean shouldFail, BlockException ex) {
        log.warn("Flow control blocked for resource: flowControlResource", ex);
        return SentinelResultDto.builder()
                .success(false)
                .resourceName("flowControlResource")
                .message("Request blocked by flow control rule")
                .blockException(ex.getClass().getSimpleName())
                .ruleType("FLOW_CONTROL")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 流量控制 Fallback 方法
     */
    public SentinelResultDto handleFlowControlFallback(boolean shouldFail, Throwable ex) {
        log.error("Flow control fallback triggered for resource: flowControlResource", ex);
        return SentinelResultDto.builder()
                .success(false)
                .resourceName("flowControlResource")
                .message("Fallback response due to exception")
                .error(ex.getClass().getSimpleName())
                .errorMessage(ex.getMessage())
                .ruleType("FLOW_CONTROL")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 熔断降级演示 - 使用 @SentinelResource 注解
     * 演示熔断器状态转换（关闭 -> 打开 -> 半开 -> 关闭）
     */
    @SentinelResource(
            value = "degradeResource",
            blockHandler = "handleDegradeBlock",
            fallback = "handleDegradeFallback"
    )
    public SentinelResultDto degradeDemo(String scenario) {
        String resourceName = "degradeResource";
        long callCount = callCounters.computeIfAbsent(resourceName, k -> new AtomicLong(0)).incrementAndGet();

        log.info("Degrade demo - scenario: {}, call count: {}", scenario, callCount);

        // 模拟不同场景
        switch (scenario.toLowerCase()) {
            case "success":
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("Request succeeded - call count: " + callCount)
                        .callCount(callCount)
                        .timestamp(LocalDateTime.now())
                        .ruleType("DEGRADE")
                        .degradeStatus("CLOSED")
                        .build();

            case "slow":
                // 慢调用场景
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("Slow request completed - call count: " + callCount)
                        .callCount(callCount)
                        .executionTimeMs(500)
                        .timestamp(LocalDateTime.now())
                        .ruleType("DEGRADE")
                        .degradeStatus("CLOSED")
                        .build();

            case "exception":
                throw new RuntimeException("Simulated business exception - call count: " + callCount);

            default:
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("Default response - call count: " + callCount)
                        .callCount(callCount)
                        .timestamp(LocalDateTime.now())
                        .ruleType("DEGRADE")
                        .build();
        }
    }

    /**
     * 熔断降级 Block Handler
     */
    public SentinelResultDto handleDegradeBlock(String scenario, BlockException ex) {
        log.warn("Degrade blocked for resource: degradeResource, scenario: {}", scenario, ex);
        return SentinelResultDto.builder()
                .success(false)
                .resourceName("degradeResource")
                .message("Request blocked by circuit breaker (degrade rule)")
                .blockException(ex.getClass().getSimpleName())
                .ruleType("DEGRADE")
                .degradeStatus("OPEN")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 熔断降级 Fallback 方法
     */
    public SentinelResultDto handleDegradeFallback(String scenario, Throwable ex) {
        log.error("Degrade fallback triggered for resource: degradeResource, scenario: {}", scenario, ex);
        return SentinelResultDto.builder()
                .success(false)
                .resourceName("degradeResource")
                .message("Fallback response due to exception")
                .error(ex.getClass().getSimpleName())
                .errorMessage(ex.getMessage())
                .ruleType("DEGRADE")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 热点参数限流演示
     * 演示基于参数的热点流控
     */
    @SentinelResource(
            value = "paramFlowResource",
            blockHandler = "handleParamFlowBlock"
    )
    public SentinelResultDto paramFlowDemo(String userId, String productId) {
        String resourceName = "paramFlowResource";
        long callCount = callCounters.computeIfAbsent(resourceName + ":" + userId, k -> new AtomicLong(0)).incrementAndGet();

        log.info("Param flow demo - userId: {}, productId: {}, call count: {}", userId, productId, callCount);

        return SentinelResultDto.builder()
                .success(true)
                .resourceName(resourceName)
                .message("Hot parameter request succeeded - userId: " + userId + ", productId: " + productId)
                .callCount(callCount)
                .parameters(Map.of("userId", userId, "productId", productId))
                .timestamp(LocalDateTime.now())
                .ruleType("PARAM_FLOW")
                .build();
    }

    /**
     * 热点参数流控 Block Handler
     */
    public SentinelResultDto handleParamFlowBlock(String userId, String productId, BlockException ex) {
        log.warn("Param flow blocked for resource: paramFlowResource, userId: {}, productId: {}", userId, productId, ex);
        return SentinelResultDto.builder()
                .success(false)
                .resourceName("paramFlowResource")
                .message("Hot parameter flow control triggered - userId: " + userId)
                .blockException(ex.getClass().getSimpleName())
                .parameters(Map.of("userId", userId, "productId", productId))
                .ruleType("PARAM_FLOW")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 系统自适应保护演示
     * 演示系统级别规则（CPU、RT、线程数、入口 QPS）
     */
    @SentinelResource(
            value = "systemRuleResource",
            blockHandler = "handleSystemRuleBlock"
    )
    public SentinelResultDto systemRuleDemo(String loadType) {
        String resourceName = "systemRuleResource";
        long callCount = callCounters.computeIfAbsent(resourceName, k -> new AtomicLong(0)).incrementAndGet();

        log.info("System rule demo - loadType: {}, call count: {}", loadType, callCount);

        // 模拟不同类型的系统负载
        switch (loadType.toLowerCase()) {
            case "cpu":
                // 模拟 CPU 密集型操作
                double result = 0;
                for (int i = 0; i < 1000000; i++) {
                    result += Math.sqrt(i);
                }
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("CPU intensive task completed")
                        .callCount(callCount)
                        .timestamp(LocalDateTime.now())
                        .ruleType("SYSTEM_RULE")
                        .systemLoadType("CPU")
                        .build();

            case "rt":
                // 模拟长耗时操作
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("Long RT task completed")
                        .callCount(callCount)
                        .executionTimeMs(100)
                        .timestamp(LocalDateTime.now())
                        .ruleType("SYSTEM_RULE")
                        .systemLoadType("RT")
                        .build();

            case "concurrency":
                // 模拟并发操作
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("Concurrent task completed")
                        .callCount(callCount)
                        .timestamp(LocalDateTime.now())
                        .ruleType("SYSTEM_RULE")
                        .systemLoadType("CONCURRENCY")
                        .build();

            default:
                return SentinelResultDto.builder()
                        .success(true)
                        .resourceName(resourceName)
                        .message("Normal task completed")
                        .callCount(callCount)
                        .timestamp(LocalDateTime.now())
                        .ruleType("SYSTEM_RULE")
                        .build();
        }
    }

    /**
     * 系统规则 Block Handler
     */
    public SentinelResultDto handleSystemRuleBlock(String loadType, BlockException ex) {
        log.warn("System rule triggered - loadType: {}", loadType, ex);
        return SentinelResultDto.builder()
                .success(false)
                .resourceName("systemRuleResource")
                .message("System protection rule triggered - loadType: " + loadType)
                .blockException(ex.getClass().getSimpleName())
                .ruleType("SYSTEM_RULE")
                .systemLoadType(loadType)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 编程式定义资源演示 - 使用 Entry 手动定义资源
     * 展示如何手动进行流量控制
     */
    public SentinelResultDto manualEntryDemo(String resourceName, int count) {
        Entry entry = null;
        try {
            // 手动定义资源并进行流控
            entry = SphU.entry(resourceName, EntryType.IN);
            long callCount = callCounters.computeIfAbsent(resourceName, k -> new AtomicLong(0)).incrementAndGet();

            log.info("Manual entry demo - resource: {}, count: {}, call count: {}", resourceName, count, callCount);

            // 模拟业务逻辑
            Thread.sleep(50);

            return SentinelResultDto.builder()
                    .success(true)
                    .resourceName(resourceName)
                    .message("Manual entry request succeeded - count: " + count)
                    .callCount(callCount)
                    .timestamp(LocalDateTime.now())
                    .ruleType("MANUAL_ENTRY")
                    .build();

        } catch (BlockException e) {
            log.warn("Manual entry blocked - resource: {}", resourceName, e);
            return SentinelResultDto.builder()
                    .success(false)
                    .resourceName(resourceName)
                    .message("Request blocked by flow control")
                    .blockException(e.getClass().getSimpleName())
                    .ruleType("MANUAL_ENTRY")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Manual entry interrupted - resource: {}", resourceName, e);
            return SentinelResultDto.builder()
                    .success(false)
                    .resourceName(resourceName)
                    .message("Request interrupted")
                    .error(e.getClass().getSimpleName())
                    .ruleType("MANUAL_ENTRY")
                    .timestamp(LocalDateTime.now())
                    .build();
        } finally {
            if (entry != null) {
                entry.exit(count);
            }
        }
    }

    /**
     * 异常追踪演示
     * 展示 Tracer.trace() 的使用
     */
    @SentinelResource(value = "exceptionTraceResource")
    public SentinelResultDto exceptionTraceDemo(boolean shouldTrace) {
        String resourceName = "exceptionTraceResource";
        long callCount = callCounters.computeIfAbsent(resourceName, k -> new AtomicLong(0)).incrementAndGet();

        log.info("Exception trace demo - shouldTrace: {}, call count: {}", shouldTrace, callCount);

        if (shouldTrace) {
            // 记录异常到 Sentinel 统计中
            Exception exception = new RuntimeException("Traced exception - call count: " + callCount);
            Tracer.trace(exception);
            throw new RuntimeException("Business exception with tracing");
        }

        return SentinelResultDto.builder()
                .success(true)
                .resourceName(resourceName)
                .message("Exception trace demo succeeded")
                .callCount(callCount)
                .timestamp(LocalDateTime.now())
                .ruleType("EXCEPTION_TRACE")
                .build();
    }

    // ==================== 规则动态管理方法 ====================

    /**
     * 动态添加流量控制规则
     */
    public void addFlowRule(String resource, int count, int grade, String limitApp) {
        List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());

        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(grade); // 0: thread count, 1: QPS
        rule.setCount(count);
        rule.setLimitApp(limitApp);
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT); // 直接拒绝
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT); // 默认快速失败

        rules.add(rule);
        FlowRuleManager.loadRules(rules);

        log.info("Added flow rule - resource: {}, count: {}, grade: {}, limitApp: {}", resource, count, grade, limitApp);
    }

    /**
     * 动态添加熔断降级规则
     */
    public void addDegradeRule(String resource, int grade, double count, int timeWindow, int minRequestAmount, int statIntervalMs) {
        List<DegradeRule> rules = new ArrayList<>(DegradeRuleManager.getRules());

        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(grade); // 0: slow call ratio, 1: exception ratio, 2: exception count
        rule.setCount(count);
        rule.setTimeWindow(timeWindow);
        rule.setMinRequestAmount(minRequestAmount);
        rule.setStatIntervalMs(statIntervalMs);

        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

        log.info("Added degrade rule - resource: {}, grade: {}, count: {}, timeWindow: {}", resource, grade, count, timeWindow);
    }

    /**
     * 动态添加热点参数流控规则
     * 注意：Sentinel 1.8.8 的 param 功能需要额外依赖，此方法仅作为占位符
     */
    public void addParamFlowRule(String resource, int threshold, int paramIdx, String paramName) {
        log.info("Param flow rules require additional dependency - resource: {}, threshold: {}, paramIdx: {}", resource, threshold, paramIdx);
        // Sentinel 1.8.8 需要 sentinel-parameter-flow-control 模块
        // 这里仅作为占位符，实际需要添加额外依赖
    }

    /**
     * 动态添加系统规则
     */
    public void addSystemRule(int ruleType, double threshold) {
        List<SystemRule> rules = new ArrayList<>();

        SystemRule rule = new SystemRule();

        // 根据规则类型设置阈值
        switch (ruleType) {
            case 0: // LOAD
                rule.setHighestSystemLoad(threshold);
                break;
            case 1: // RT
                rule.setAvgRt((long) threshold);
                break;
            case 2: // THREAD
                rule.setMaxThread((int) threshold);
                break;
            case 3: // QPS
                rule.setQps(threshold);
                break;
            case 4: // CPU usage
                rule.setHighestCpuUsage(threshold);
                break;
        }

        rules.add(rule);
        SystemRuleManager.loadRules(rules);

        log.info("Added system rule - ruleType: {}, threshold: {}", ruleType, threshold);
    }

    /**
     * 删除指定资源的流量控制规则
     */
    public void removeFlowRule(String resource) {
        List<FlowRule> rules = FlowRuleManager.getRules().stream()
                .filter(rule -> !rule.getResource().equals(resource))
                .collect(Collectors.toList());

        FlowRuleManager.loadRules(rules);
        log.info("Removed flow rule for resource: {}", resource);
    }

    /**
     * 删除指定资源的熔断降级规则
     */
    public void removeDegradeRule(String resource) {
        List<DegradeRule> rules = DegradeRuleManager.getRules().stream()
                .filter(rule -> !rule.getResource().equals(resource))
                .collect(Collectors.toList());

        DegradeRuleManager.loadRules(rules);
        log.info("Removed degrade rule for resource: {}", resource);
    }

    /**
     * 删除热点参数流控规则
     * 注意：Sentinel 1.8.8 的 param 功能需要额外依赖
     */
    public void removeParamFlowRule(String resource) {
        log.info("Param flow rules require additional dependency - resource: {}", resource);
        // Sentinel 1.8.8 需要 sentinel-parameter-flow-control 模块
    }

    /**
     * 清除所有系统规则
     */
    public void clearSystemRules() {
        SystemRuleManager.loadRules(new ArrayList<>());
        log.info("Cleared all system rules");
    }

    /**
     * 清除所有规则
     */
    public void clearAllRules() {
        FlowRuleManager.loadRules(new ArrayList<>());
        DegradeRuleManager.loadRules(new ArrayList<>());
        SystemRuleManager.loadRules(new ArrayList<>());
        log.info("Cleared all Sentinel rules");
    }

    // ==================== 统计信息收集方法 ====================

    /**
     * 获取资源的实时统计信息
     * 注意：由于 Sentinel 1.8.8 的 API 限制，此方法返回基于本地计数器的统计数据
     */
    public SentinelMetricsDto getResourceMetrics(String resourceName) {
        // 使用本地计数器获取统计数据
        AtomicLong callCounter = callCounters.get(resourceName);
        AtomicLong failureCounter = failureCounters.get(resourceName);

        long callCount = callCounter != null ? callCounter.get() : 0;
        long failureCount = failureCounter != null ? failureCounter.get() : 0;

        // 计算统计数据
        long totalRequest = callCount;
        long exceptionCount = failureCount;
        long passQps = callCount; // 简化处理，实际需要时间窗口
        long blockQps = 0; // 从规则管理器无法直接获取
        long exceptionQps = failureCount;
        double successRate = totalRequest > 0 ? (double) (totalRequest - exceptionCount) / totalRequest * 100 : 100;
        long rt = 0; // 默认值

        return SentinelMetricsDto.builder()
                .resourceName(resourceName)
                .passQps(passQps)
                .blockQps(blockQps)
                .totalRequest(totalRequest)
                .exceptionQps(exceptionQps)
                .successRate(String.format("%.2f%%", successRate))
                .averageRt(rt)
                .concurrency(0) // 无法从公开 API 获取
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 获取所有已加载的规则信息
     */
    public List<RuleInfoDto> getAllRules() {
        List<RuleInfoDto> ruleInfos = new ArrayList<>();

        // 流量控制规则
        for (FlowRule rule : FlowRuleManager.getRules()) {
            ruleInfos.add(RuleInfoDto.builder()
                    .resource(rule.getResource())
                    .ruleType("FLOW")
                    .limitApp(rule.getLimitApp())
                    .grade(rule.getGrade() == 0 ? "THREAD_COUNT" : "QPS")
                    .count(String.valueOf(rule.getCount()))
                    .strategy(String.valueOf(rule.getStrategy()))
                    .controlBehavior(String.valueOf(rule.getControlBehavior()))
                    .build());
        }

        // 熔断降级规则
        for (DegradeRule rule : DegradeRuleManager.getRules()) {
            String gradeStr;
            switch (rule.getGrade()) {
                case 0:
                    gradeStr = "SLOW_CALL_RATIO";
                    break;
                case 1:
                    gradeStr = "EXCEPTION_RATIO";
                    break;
                case 2:
                    gradeStr = "EXCEPTION_COUNT";
                    break;
                default:
                    gradeStr = "UNKNOWN";
            }

            ruleInfos.add(RuleInfoDto.builder()
                    .resource(rule.getResource())
                    .ruleType("DEGRADE")
                    .grade(gradeStr)
                    .count(String.valueOf(rule.getCount()))
                    .timeWindow(String.valueOf(rule.getTimeWindow()))
                    .minRequestAmount(String.valueOf(rule.getMinRequestAmount()))
                    .statIntervalMs(String.valueOf(rule.getStatIntervalMs()))
                    .build());
        }

        // 系统规则 - Sentinel 1.8.8 中 SystemRuleManager 可能没有 getSystemRules 方法
        try {
            // 使用反射获取系统规则（如果 API 不可用）
            java.lang.reflect.Field rulesField = SystemRuleManager.class.getDeclaredField("systemRules");
            rulesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<SystemRule> systemRules = (List<SystemRule>) rulesField.get(null);
            for (SystemRule rule : systemRules) {
                ruleInfos.add(RuleInfoDto.builder()
                        .resource("SYSTEM")
                        .ruleType("SYSTEM")
                        .highestSystemLoad(String.valueOf(rule.getHighestSystemLoad()))
                        .highestCpuUsage(String.valueOf(rule.getHighestCpuUsage()))
                        .maxRt(String.valueOf(rule.getAvgRt()))
                        .concurrency(String.valueOf(rule.getMaxThread()))
                        .qps(String.valueOf(rule.getQps()))
                        .build());
            }
        } catch (Exception e) {
            log.debug("Unable to retrieve system rules: {}", e.getMessage());
        }

        return ruleInfos;
    }

    /**
     * 获取所有资源的名称列表
     */
    public Set<String> getAllResourceNames() {
        Set<String> resourceNames = new HashSet<>();

        // 从流控规则中获取
        FlowRuleManager.getRules().forEach(rule -> resourceNames.add(rule.getResource()));

        // 从熔断规则中获取
        DegradeRuleManager.getRules().forEach(rule -> resourceNames.add(rule.getResource()));

        // 添加已知的资源名称
        resourceNames.addAll(Arrays.asList(
                "flowControlResource",
                "degradeResource",
                "paramFlowResource",
                "systemRuleResource",
                "exceptionTraceResource"
        ));

        return resourceNames;
    }

    /**
     * 重置计数器
     */
    public void resetCounters() {
        callCounters.clear();
        failureCounters.clear();
        log.info("All counters have been reset");
    }

    /**
     * 获取指定资源的计数器值
     */
    public Map<String, Long> getCounters(String resource) {
        Map<String, Long> counterMap = new HashMap<>();
        AtomicLong callCounter = callCounters.get(resource);
        AtomicLong failureCounter = failureCounters.get(resource);

        if (callCounter != null) {
            counterMap.put("callCount", callCounter.get());
        }
        if (failureCounter != null) {
            counterMap.put("failureCount", failureCounter.get());
        }

        return counterMap;
    }
}
