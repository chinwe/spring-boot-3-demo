package com.example.demo.sentinel.configuration;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sentinel 配置类
 * 配置 Sentinel 的基础规则和初始化
 */
@Slf4j
@Configuration
public class SentinelConfiguration {

    @Value("${sentinel.flow.default-qps:10}")
    private int defaultQpsThreshold;

    @Value("${sentinel.flow.default-thread-count:5}")
    private int defaultThreadCount;

    @Value("${sentinel.degrade.default-min-request:5}")
    private int defaultMinRequest;

    @Value("${sentinel.degrade.default-time-window:10}")
    private int defaultTimeWindow;

    @Value("${sentinel.degrade.default-slow-ratio:0.5}")
    private double defaultSlowRatio;

    @Value("${sentinel.system.max-cpu-usage:0.8}")
    private double maxCpuUsage;

    @Value("${sentinel.enabled:true}")
    private boolean sentinelEnabled;

    /**
     * 初始化 Sentinel 规则
     */
    @PostConstruct
    public void initSentinelRules() {
        if (!sentinelEnabled) {
            log.info("Sentinel is disabled by configuration");
            return;
        }

        log.info("Initializing Sentinel with default rules...");

        // 初始化流控规则
        initFlowRules();

        // 初始化降级规则
        initDegradeRules();

        // 初始化系统规则
        initSystemRules();

        log.info("Sentinel initialized successfully with default QPS threshold: {}, default thread count: {}",
                defaultQpsThreshold, defaultThreadCount);
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 示例：为默认资源设置流控规则
        FlowRule defaultRule = new FlowRule();
        defaultRule.setResource("sentinel-flow-control-demo");
        defaultRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        defaultRule.setCount(defaultQpsThreshold);
        defaultRule.setLimitApp("default");
        defaultRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        defaultRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(defaultRule);

        // 添加热点参数流控示例
        FlowRule hotspotRule = new FlowRule();
        hotspotRule.setResource("sentinel-hotspot-demo");
        hotspotRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        hotspotRule.setCount(defaultQpsThreshold / 2);
        hotspotRule.setLimitApp("default");
        hotspotRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rules.add(hotspotRule);

        FlowRuleManager.loadRules(rules);
        log.info("Flow rules initialized: {} rules loaded", rules.size());
    }

    /**
     * 初始化降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 慢调用比例降级规则
        DegradeRule slowRatioRule = new DegradeRule();
        slowRatioRule.setResource("sentinel-degrade-demo");
        slowRatioRule.setGrade(0); // DEGRADE_GRADE_SLOW_REQUEST_RATIO
        slowRatioRule.setCount(defaultSlowRatio);
        slowRatioRule.setTimeWindow(defaultTimeWindow);
        slowRatioRule.setMinRequestAmount(defaultMinRequest);
        slowRatioRule.setStatIntervalMs(10000); // 10秒统计窗口
        rules.add(slowRatioRule);

        // 异常比例降级规则
        DegradeRule exceptionRatioRule = new DegradeRule();
        exceptionRatioRule.setResource("sentinel-exception-demo");
        exceptionRatioRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        exceptionRatioRule.setCount(0.5); // 50% 异常比例
        exceptionRatioRule.setTimeWindow(defaultTimeWindow);
        exceptionRatioRule.setMinRequestAmount(defaultMinRequest);
        exceptionRatioRule.setStatIntervalMs(10000);
        rules.add(exceptionRatioRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Degrade rules initialized: {} rules loaded", rules.size());
    }

    /**
     * 初始化系统规则
     */
    private void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        // CPU 使用率规则
        SystemRule cpuRule = new SystemRule();
        cpuRule.setHighestSystemLoad(0.8);
        rules.add(cpuRule);

        // 平均 RT 规则
        SystemRule rtRule = new SystemRule();
        rtRule.setAvgRt(1000); // 1000ms
        rules.add(rtRule);

        // 并发线程数规则
        SystemRule threadRule = new SystemRule();
        threadRule.setMaxThread(defaultThreadCount);
        rules.add(threadRule);

        // QPS 规则
        SystemRule qpsRule = new SystemRule();
        qpsRule.setQps(defaultQpsThreshold * 2);
        rules.add(qpsRule);

        SystemRuleManager.loadRules(rules);
        log.info("System rules initialized: {} rules loaded", rules.size());
    }

    /**
     * 动态添加流控规则
     */
    public void addFlowRule(String resource, int threshold) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(threshold);
        rule.setLimitApp("default");
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());
        rules.add(rule);
        FlowRuleManager.loadRules(rules);

        log.info("Flow rule added for resource: {} with threshold: {}", resource, threshold);
    }

    /**
     * 动态移除流控规则
     */
    public void removeFlowRule(String resource) {
        List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());
        rules.removeIf(rule -> rule.getResource().equals(resource));
        FlowRuleManager.loadRules(rules);

        log.info("Flow rule removed for resource: {}", resource);
    }

    /**
     * 获取所有流控规则
     */
    public List<FlowRule> getFlowRules() {
        return FlowRuleManager.getRules() != null ?
                FlowRuleManager.getRules() : Collections.emptyList();
    }

    /**
     * 获取所有降级规则
     */
    public List<DegradeRule> getDegradeRules() {
        return DegradeRuleManager.getRules() != null ?
                DegradeRuleManager.getRules() : Collections.emptyList();
    }
}
