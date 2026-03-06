package com.example.demo.metrics.binder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sentinel 流量控制和熔断指标收集器
 * 使用 Micrometer 记录 Sentinel 的执行情况
 */
@Slf4j
@Component
public class SentinelMetrics {

    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, CircuitBreakerState> circuitStates;

    public SentinelMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.circuitStates = new ConcurrentHashMap<>();

        log.info("SentinelMetrics initialized with Micrometer registry");
    }

    /**
     * 记录请求被阻塞
     */
    public void recordBlocked(String resource, String ruleType) {
        Counter.builder("sentinel_blocked_total")
                .description("Total number of requests blocked by Sentinel")
                .tag("resource", resource)
                .tag("rule_type", ruleType)
                .register(registry)
                .increment();

        log.debug("Sentinel blocked request - resource: {}, rule type: {}", resource, ruleType);
    }

    /**
     * 记录请求通过
     */
    public void recordPassed(String resource) {
        Counter.builder("sentinel_passed_total")
                .description("Total number of requests passed by Sentinel")
                .tag("resource", resource)
                .register(registry)
                .increment();

        log.debug("Sentinel passed request - resource: {}", resource);
    }

    /**
     * 更新熔断器状态
     */
    public void updateCircuitState(String resource, String state) {
        CircuitBreakerState circuitState = circuitStates.computeIfAbsent(
                resource,
                k -> new CircuitBreakerState(resource)
        );
        circuitState.setState(state);
        log.debug("Sentinel circuit state updated - resource: {}, state: {}", resource, state);
    }

    /**
     * 获取熔断器状态（用于 Gauge）
     */
    public int getCircuitStateValue(String resource) {
        CircuitBreakerState state = circuitStates.get(resource);
        if (state == null) {
            return 0; // CLOSED
        }
        return switch (state.getState()) {
            case "open" -> 2;
            case "half_open" -> 1;
            default -> 0; // closed
        };
    }

    /**
     * 为资源注册熔断器状态 Gauge
     */
    public void registerCircuitStateGauge(String resource) {
        CircuitBreakerState state = circuitStates.computeIfAbsent(
                resource,
                k -> new CircuitBreakerState(resource)
        );

        Gauge.builder("sentinel_circuit_state", state, s -> getStateValue(s.getState()))
                .description("Circuit breaker state (0=closed, 1=half_open, 2=open)")
                .tag("resource", resource)
                .tag("state", state.getState())
                .register(registry);
    }

    private double getStateValue(String state) {
        return switch (state) {
            case "open" -> 2;
            case "half_open" -> 1;
            default -> 0;
        };
    }

    /**
     * 熔断器状态内部类
     */
    private static class CircuitBreakerState {
        private final String resource;
        private volatile String state = "closed";

        public CircuitBreakerState(String resource) {
            this.resource = resource;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
