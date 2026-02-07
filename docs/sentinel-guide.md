# Sentinel 流量控制与熔断降级使用指南

## 目录

- [简介](#简介)
- [核心概念](#核心概念)
- [快速开始](#快速开始)
- [流量控制](#流量控制)
- [熔断降级](#熔断降级)
- [热点参数限流](#热点参数限流)
- [系统自适应保护](#系统自适应保护)
- [规则动态管理](#规则动态管理)
- [API 接口文档](#api-接口文档)
- [配置说明](#配置说明)
- [最佳实践](#最佳实践)

---

## 简介

Alibaba Sentinel 是面向分布式服务架构的流量控制组件，主要以流量为切入点，从**流量控制**、**熔断降级**、**系统负载保护**等多个维度来帮助您保障微服务的稳定性。

### 主要特性

| 特性 | 说明 |
|------|------|
| **流量控制** | 基于 QPS/并发线程数的流量控制，支持 Warm Up、排队等待等策略 |
| **熔断降级** | 基于响应时间、异常比例、异常数的熔断降级 |
| **热点参数限流** | 基于参数值的精细化流控（如对频繁用户限流） |
| **系统自适应保护** | 根据 CPU 使用率、平均 RT、并发线程数等系统指标进行保护 |
| **实时监控** | 实时统计和监控接口调用情况 |

### 项目集成版本

```xml
<sentinel.version>1.8.8</sentinel.version>
```

---

## 核心概念

### 资源 (Resource)

资源是 Sentinel 的核心概念，它可以是 Java 应用程序中的任何内容，例如：

- 服务提供者提供的服务
- 服务消费者调用的方法
- 甚至是一段代码

只要被 Sentinel 定义的资源，就可以配置流控规则、熔断降级规则等。

### 规则 (Rule)

Sentinel 支持多种规则类型：

| 规则类型 | 说明 | 配置类 |
|----------|------|--------|
| **流控规则** | 限制 QPS 或并发线程数 | `FlowRule` |
| **熔断规则** | 根据异常比例或慢调用比例进行熔断 | `DegradeRule` |
| **系统规则** | 根据系统指标进行保护 | `SystemRule` |
| **热点参数规则** | 针对参数值的精细化流控 | `ParamFlowRule` |

### 基于注解的资源定义

使用 `@SentinelResource` 注解定义资源：

```java
@SentinelResource(
    value = "resourceName",
    blockHandler = "handleBlock",      // 限流/熔断时的处理方法
    fallback = "handleFallback"        // 异常时的降级方法
)
public String doSomething(String param) {
    // 业务逻辑
}
```

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.8</version>
</dependency>
```

### 2. 基本使用

```java
@Service
public class MyService {

    @SentinelResource(value = "myResource", blockHandler = "handleBlock")
    public String process(String input) {
        // 业务逻辑
        return "processed: " + input;
    }

    // 限流降级处理
    public String handleBlock(String input, BlockException ex) {
        return "blocked: " + input;
    }
}
```

### 3. 配置流控规则

```java
// 配置 QPS 流控规则
List<FlowRule> rules = new ArrayList<>();
FlowRule rule = new FlowRule();
rule.setResource("myResource");
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
rule.setCount(10);  // 每秒最多 10 次请求
rules.add(rule);
FlowRuleManager.loadRules(rules);
```

---

## 流量控制

### 流量控制模式

Sentinel 支持多种流量控制模式：

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| **直接拒绝** | 超过阈值直接拒绝 | 默认模式，简单直接 |
| **Warm Up** | 预热模式，阈值缓慢增加 | 秒杀系统、冷启动保护 |
| **匀速排队** | 匀速通过请求 | 削峰填谷 |
| **预热 + 排队** | 组合模式 | 复杂场景 |

### QPS 流量控制

```java
@SentinelResource(
    value = "flowControlResource",
    blockHandler = "handleFlowControlBlock"
)
public SentinelResultDto flowControlDemo(boolean shouldFail) {
    // 业务逻辑
    return buildSuccessResult();
}

public SentinelResultDto handleFlowControlBlock(boolean shouldFail, BlockException ex) {
    return buildBlockedResult("Request blocked by flow control");
}
```

**配置 QPS 限流规则：**

```java
FlowRule rule = new FlowRule();
rule.setResource("flowControlResource");
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);  // QPS 限流
rule.setCount(10);  // 阈值：每秒 10 次
rule.setStrategy(RuleConstant.STRATEGY_DIRECT);  // 直接拒绝
rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
```

### 并发线程数流量控制

```java
FlowRule rule = new FlowRule();
rule.setResource("flowControlResource");
rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);  // 线程数限流
rule.setCount(5);  // 最大并发线程数：5
```

### Warm Up（预热）模式

适用于秒杀系统等场景，在系统启动初期缓慢增加阈值：

```java
FlowRule rule = new FlowRule();
rule.setResource("warmUpResource");
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
rule.setCount(100);  // 最终阈值
rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
rule.setWarmUpPeriodSec(10);  // 预热时长 10 秒
```

### 编程式资源定义

使用 `SphU.entry()` 手动定义资源：

```java
public SentinelResultDto manualEntryDemo(String resourceName) {
    Entry entry = null;
    try {
        entry = SphU.entry(resourceName, EntryType.IN);
        // 业务逻辑
        return buildSuccessResult();
    } catch (BlockException e) {
        return buildBlockedResult();
    } finally {
        if (entry != null) {
            entry.exit();
        }
    }
}
```

---

## 熔断降级

### 熔断器状态

熔断器有三个状态：

```
┌─────────┐  失败率/慢调用达到阈值   ┌─────────┐
│  关闭   │  ─────────────────────> │   打开  │
│ (CLOSED) │                        │  (OPEN) │
└─────────┘                        └─────────┘
     ^                                    │
     │            熔断时长后               │
     └────────────────────────────────────┘
                    探测请求成功
                  ┌─────────┐
                  │  半开   │
                  │(HALF_OPEN)│
                  └─────────┘
```

### 慢调用比例熔断

当响应时间超过阈值的比例达到设定值时触发熔断：

```java
@SentinelResource(value = "degradeResource", blockHandler = "handleDegradeBlock")
public String processWithSlowCallCheck() {
    // 模拟慢调用
    Thread.sleep(500);
    return "done";
}

// 配置慢调用比例熔断规则
DegradeRule rule = new DegradeRule();
rule.setResource("degradeResource");
rule.setGrade(0);  // 慢调用比例
rule.setCount(0.5);  // 慢调用比例阈值：50%
rule.setTimeWindow(10);  // 熔断时长：10秒
rule.setMinRequestAmount(5);  // 最小请求数：5
rule.setStatIntervalMs(10000);  // 统计窗口：10秒
rule.setSlowRatioThreshold(500);  // 慢调用阈值：500ms
```

### 异常比例熔断

```java
DegradeRule rule = new DegradeRule();
rule.setResource("degradeResource");
rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);  // 异常比例
rule.setCount(0.5);  // 异常比例阈值：50%
rule.setTimeWindow(10);  // 熔断时长：10秒
rule.setMinRequestAmount(5);  // 最小请求数
```

### 异常数熔断

```java
DegradeRule rule = new DegradeRule();
rule.setResource("degradeResource");
rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);  // 异常数
rule.setCount(10);  // 异常数阈值
rule.setTimeWindow(60);  // 时间窗口：60秒
```

---

## 热点参数限流

热点参数限流可以针对请求中的参数进行精细化流控，例如：

- 对特定用户 ID 进行限流
- 对特定商品 ID 进行限流

```java
@SentinelResource(
    value = "paramFlowResource",
    blockHandler = "handleParamFlowBlock"
)
public String processRequest(String userId, String productId) {
    // 业务逻辑
    return "processed for user: " + userId;
}

public String handleParamFlowBlock(String userId, String productId, BlockException ex) {
    return "blocked for user: " + userId;
}
```

> **注意**：Sentinel 1.8.8 的热点参数限流需要额外依赖 `sentinel-parameter-flow-control` 模块。

---

## 系统自适应保护

系统规则从应用级别的入口流量进行控制，从单台机器的 **Load**、**CPU 使用率**、**平均 RT**、**并发线程数** 和 **入口 QPS** 五个维度进行保护。

### CPU 使用率保护

```java
SystemRule rule = new SystemRule();
rule.setHighestCpuUsage(0.8);  // CPU 使用率超过 80% 时触发保护
```

### 平均 RT 保护

```java
SystemRule rule = new SystemRule();
rule.setAvgRt(1000);  // 平均响应时间超过 1000ms 时触发保护
```

### 并发线程数保护

```java
SystemRule rule = new SystemRule();
rule.setMaxThread(100);  // 并发线程数超过 100 时触发保护
```

### 系统入口 QPS 保护

```java
SystemRule rule = new SystemRule();
rule.setQps(1000);  // 入口 QPS 超过 1000 时触发保护
```

### 系统负载保护

```java
SystemRule rule = new SystemRule();
rule.setHighestSystemLoad(10.0);  // 系统负载超过 10 时触发保护
```

---

## 规则动态管理

### 动态添加流控规则

```java
public void addFlowRule(String resource, int count, int grade, String limitApp) {
    List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());

    FlowRule rule = new FlowRule();
    rule.setResource(resource);
    rule.setGrade(grade);  // 0: 线程数, 1: QPS
    rule.setCount(count);
    rule.setLimitApp(limitApp);
    rule.setStrategy(RuleConstant.STRATEGY_DIRECT);

    rules.add(rule);
    FlowRuleManager.loadRules(rules);
}
```

### 动态添加熔断规则

```java
public void addDegradeRule(String resource, int grade, double count,
                           int timeWindow, int minRequestAmount) {
    List<DegradeRule> rules = new ArrayList<>(DegradeRuleManager.getRules());

    DegradeRule rule = new DegradeRule();
    rule.setResource(resource);
    rule.setGrade(grade);  // 0: 慢调用比例, 1: 异常比例, 2: 异常数
    rule.setCount(count);
    rule.setTimeWindow(timeWindow);
    rule.setMinRequestAmount(minRequestAmount);

    rules.add(rule);
    DegradeRuleManager.loadRules(rules);
}
```

### 删除规则

```java
// 删除流控规则
public void removeFlowRule(String resource) {
    List<FlowRule> rules = FlowRuleManager.getRules().stream()
        .filter(rule -> !rule.getResource().equals(resource))
        .collect(Collectors.toList());
    FlowRuleManager.loadRules(rules);
}

// 删除熔断规则
public void removeDegradeRule(String resource) {
    List<DegradeRule> rules = DegradeRuleManager.getRules().stream()
        .filter(rule -> !rule.getResource().equals(resource))
        .collect(Collectors.toList());
    DegradeRuleManager.loadRules(rules);
}
```

### 清除所有规则

```java
public void clearAllRules() {
    FlowRuleManager.loadRules(new ArrayList<>());
    DegradeRuleManager.loadRules(new ArrayList<>());
    SystemRuleManager.loadRules(new ArrayList<>());
}
```

---

## API 接口文档

### 基础信息

- **Base URL**: `http://localhost:8080/api/sentinel`
- **API 版本**: 1.0.0
- **Sentinel 版本**: 1.8.8

### 流量控制接口

#### 1. 流量控制演示

```http
GET /api/sentinel/flow-control?shouldFail=false
```

**参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shouldFail | boolean | 否 | 是否模拟业务失败，默认 false |

**响应示例：**

```json
{
  "success": true,
  "resourceName": "flowControlResource",
  "message": "Flow control request succeeded - call count: 1",
  "callCount": 1,
  "timestamp": "2026-02-07T19:30:00",
  "ruleType": "FLOW_CONTROL"
}
```

#### 2. 手动 Entry 流量控制

```http
GET /api/sentinel/flow-control/manual?resourceName=manualResource&count=1
```

### 熔断降级接口

#### 1. 熔断降级演示

```http
GET /api/sentinel/degrade?scenario=success
```

**参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scenario | string | 否 | 场景类型：success/slow/exception |

**响应示例：**

```json
{
  "success": true,
  "resourceName": "degradeResource",
  "message": "Request succeeded",
  "degradeStatus": "CLOSED",
  "ruleType": "DEGRADE"
}
```

#### 2. 慢调用熔断演示

```http
GET /api/sentinel/degrade/slow-call
```

#### 3. 异常比例熔断演示

```http
GET /api/sentinel/degrade/exception-ratio?throwException=false
```

### 热点参数限流接口

#### 1. 热点参数限流演示

```http
GET /api/sentinel/hotspot?userId=user123&productId=product456
```

**参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | string | 否 | 用户 ID（热点参数） |
| productId | string | 否 | 商品 ID |

#### 2. 频繁用户限流演示

```http
GET /api/sentinel/hotspot/frequent-user?userId=userVIP
```

### 系统保护接口

#### 1. CPU 保护演示

```http
GET /api/sentinel/system/cpu
```

#### 2. RT 保护演示

```http
GET /api/sentinel/system/rt
```

#### 3. 并发保护演示

```http
GET /api/sentinel/system/concurrency
```

#### 4. QPS 保护演示

```http
GET /api/sentinel/system/qps
```

### 规则管理接口

#### 1. 获取所有规则

```http
GET /api/sentinel/rules
```

**响应示例：**

```json
[
  {
    "resource": "flowControlResource",
    "ruleType": "FLOW",
    "grade": "QPS",
    "count": "10",
    "strategy": "0",
    "controlBehavior": "0"
  },
  {
    "resource": "degradeResource",
    "ruleType": "DEGRADE",
    "grade": "SLOW_CALL_RATIO",
    "count": "0.5",
    "timeWindow": "10"
  }
]
```

#### 2. 添加流控规则

```http
POST /api/sentinel/rules/flow
Content-Type: application/x-www-form-urlencoded

resource=flowControlResource&count=10&grade=1&limitApp=default
```

#### 3. 添加熔断规则

```http
POST /api/sentinel/rules/degrade
Content-Type: application/x-www-form-urlencoded

resource=degradeResource&grade=0&count=0.5&timeWindow=10&minRequestAmount=5
```

#### 4. 添加系统规则

```http
POST /api/sentinel/rules/system
Content-Type: application/x-www-form-urlencoded

ruleType=4&threshold=0.8
```

**系统规则类型：**

| ruleType | 说明 | threshold 说明 |
|----------|------|---------------|
| 0 | LOAD | 系统负载 |
| 1 | RT | 平均响应时间 (ms) |
| 2 | THREAD | 并发线程数 |
| 3 | QPS | 入口 QPS |
| 4 | CPU | CPU 使用率 |

#### 5. 删除流控规则

```http
DELETE /api/sentinel/rules/flow/{resource}
```

#### 6. 删除熔断规则

```http
DELETE /api/sentinel/rules/degrade/{resource}
```

#### 7. 清除所有规则

```http
DELETE /api/sentinel/rules/all
```

### 统计信息接口

#### 1. 获取资源统计信息

```http
GET /api/sentinel/statistics?resourceName=flowControlResource
```

**响应示例：**

```json
{
  "resourceName": "flowControlResource",
  "passQps": 10,
  "blockQps": 0,
  "totalRequest": 100,
  "exceptionQps": 2,
  "successRate": "98.00%",
  "averageRt": 50,
  "concurrency": 5,
  "timestamp": "2026-02-07T19:30:00"
}
```

#### 2. 获取所有资源统计

```http
GET /api/sentinel/statistics/all
```

#### 3. 获取计数器

```http
GET /api/sentinel/counters?resource=flowControlResource
```

#### 4. 重置计数器

```http
POST /api/sentinel/counters/reset
```

#### 5. 获取所有资源名称

```http
GET /api/sentinel/resources
```

### 综合演示接口

#### 获取所有功能概览

```http
GET /api/sentinel/demo-all
```

返回包含所有功能特性、API 端点列表和使用示例的完整信息。

---

## 配置说明

### application.properties 配置

```properties
# Sentinel 基础配置
sentinel.enabled=true

# 流量控制配置
sentinel.flow.default-qps=10
sentinel.flow.default-thread-count=5

# 熔断降级配置
sentinel.degrade.default-min-request=5
sentinel.degrade.default-time-window=10
sentinel.degrade.default-slow-ratio=0.5

# 系统保护配置
sentinel.system.max-cpu-usage=0.8
```

### Java 配置类

```java
@Configuration
public class SentinelConfiguration {

    @Value("${sentinel.flow.default-qps:10}")
    private int defaultQpsThreshold;

    @PostConstruct
    public void initSentinelRules() {
        // 初始化流控规则
        initFlowRules();
        // 初始化降级规则
        initDegradeRules();
        // 初始化系统规则
        initSystemRules();
    }
}
```

---

## 最佳实践

### 1. 资源命名规范

```java
// 推荐的命名方式：应用名:模块名:操作名
@SentinelResource(value = "order-service:create:submit")
public void submitOrder(OrderRequest request) {
    // ...
}

// 或者更简洁的层级命名
@SentinelResource(value = "order:submit")
public void submitOrder(OrderRequest request) {
    // ...
}
```

### 2. BlockHandler 最佳实践

```java
@SentinelResource(
    value = "orderResource",
    blockHandler = "handleBlock",
    blockHandlerClass = OrderBlockHandler.class  // 推荐使用独立的 BlockHandler 类
)
public OrderResult processOrder(OrderRequest request) {
    // ...
}

// 独立的 BlockHandler 类（必须是静态方法）
public class OrderBlockHandler {
    public static OrderResult handleBlock(OrderRequest request, BlockException ex) {
        // 统一的降级处理逻辑
        return OrderResult.fallback("Too many requests");
    }
}
```

### 3. Fallback 最佳实践

```java
@SentinelResource(
    value = "orderResource",
    fallback = "handleFallback",
    fallbackClass = OrderFallbackHandler.class  // 推荐使用独立的 Fallback 类
)
public OrderResult processOrder(OrderRequest request) {
    // ...
}

// 独立的 Fallback 类
public class OrderFallbackHandler {
    public static OrderResult handleFallback(OrderRequest request, Throwable ex) {
        // 统一的异常处理逻辑
        return OrderResult.error(ex.getMessage());
    }
}
```

### 4. 规则配置建议

| 场景 | 建议阈值类型 | 建议阈值范围 | 建议控制行为 |
|------|-------------|-------------|-------------|
| 高并发读接口 | QPS | 根据系统容量设定 | 直接拒绝 |
| 写入接口 | 并发线程数 | 数据库连接池的 50% | 排队等待 |
| 第三方接口 | 并发线程数 | 根据第三方限制 | 直接拒绝 |
| 秒杀活动 | QPS + Warm Up | 预估峰值 | Warm Up |
| 核心接口 | 系统规则 | CPU < 70% | 优先保护 |

### 5. 监控建议

- 定期查看资源统计信息
- 设置合理的告警阈值
- 关注熔断器的状态变化
- 监控系统指标

```java
// 定期检查统计信息
@Scheduled(fixedRate = 60000)  // 每分钟检查一次
public void monitorResources() {
    Set<String> resources = getAllResourceNames();
    for (String resource : resources) {
        SentinelMetricsDto metrics = getResourceMetrics(resource);
        log.info("Resource: {}, QPS: {}, BlockQPS: {}, SuccessRate: {}",
            resource, metrics.getPassQps(), metrics.getBlockQps(), metrics.getSuccessRate());
    }
}
```

### 6. 异常处理

```java
// 使用 Tracer.trace() 追踪业务异常
@SentinelResource(value = "businessResource")
public Result doBusiness(String input) {
    try {
        // 业务逻辑
        return Result.success(process(input));
    } catch (BusinessException e) {
        // 追踪业务异常到 Sentinel 统计
        Tracer.trace(e);
        throw e;
    }
}
```

---

## 附录

### A. 规则常量说明

#### FlowGrade（流控类型）

```java
RuleConstant.FLOW_GRADE_THREAD = 0   // 并发线程数限流
RuleConstant.FLOW_GRADE_QPS = 1       // QPS 限流
```

#### ControlBehavior（流控效果）

```java
RuleConstant.CONTROL_BEHAVIOR_DEFAULT = 0        // 快速失败
RuleConstant.CONTROL_BEHAVIOR_WARM_UP = 1        // Warm Up 预热
RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER = 2   // 匀速排队
RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER = 3  // Warm Up + 排队
```

#### Strategy（流控策略）

```java
RuleConstant.STRATEGY_DIRECT = 0         // 直接拒绝
RuleConstant.STRATEGY_RELATE = 1         // 关联限流
RuleConstant.STRATEGY_CHAIN = 2          // 链路限流
```

#### DegradeGrade（熔断策略）

```java
RuleConstant.DEGRADE_GRADE_SLOW_REQUEST_RATIO = 0  // 慢调用比例
RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO = 1     // 异常比例
RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT = 2     // 异常数
```

### B. 错误代码说明

| HTTP 状态码 | 说明 | 触发条件 |
|------------|------|----------|
| 200 OK | 请求成功 | 请求未被拦截 |
| 429 Too Many Requests | 被流控拦截 | 超过 QPS/线程数阈值 |
| 503 Service Unavailable | 被熔断/系统规则拦截 | 熔断器打开或系统保护触发 |
| 500 Internal Server Error | 业务异常 | 业务逻辑抛出异常 |

### C. 相关资源

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Sentinel GitHub](https://github.com/alibaba/Sentinel)
- [Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

---

*文档版本：1.0.0*
*更新时间：2026-02-07*
