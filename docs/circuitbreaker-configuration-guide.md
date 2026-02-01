# Resilience4j 配置方式使用指南

## 概述

Resilience4j 支持两种配置方式：

| 方式 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **配置文件** | 集中管理、易于修改、无需重新编译 | 灵活性稍低 | 生产环境推荐 |
| **编程方式** | 完全灵活、动态配置 | 代码侵入、需重新编译 | 特殊需求场景 |

## 配置文件结构

```
resilience4j.
├── circuitbreaker.
│   ├── configs.           # 共享配置模板
│   └── instances.         # 具体实例配置
├── ratelimiter.
│   ├── configs.
│   └── instances.
├── bulkhead.
│   ├── configs.
│   └── instances.
└── timelimiter.
    ├── configs.
    └── instances.
```

## 完整配置示例

### 方式一：application.properties

```properties
# ========== Resilience4j 配置方式示例 ==========

# ============================
# 1. 共享配置 (Shared Configs)
# ============================

# 默认熔断器配置
resilience4j.circuitbreaker.configs.default.sliding-window-size=100
resilience4j.circuitbreaker.configs.default.minimum-number-of-calls=10
resilience4j.circuitbreaker.configs.default.failure-rate-threshold=50
resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.configs.default.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.configs.default.automatic-transition-from-open-to-half-open-enabled=true
resilience4j.circuitbreaker.configs.default.slow-call-rate-threshold=50
resilience4j.circuitbreaker.configs.default.slow-call-duration-threshold=3s

# 宽松熔断器配置（用于重要服务）
resilience4j.circuitbreaker.configs.relaxed.sliding-window-size=200
resilience4j.circuitbreaker.configs.relaxed.minimum-number-of-calls=20
resilience4j.circuitbreaker.configs.relaxed.failure-rate-threshold=60
resilience4j.circuitbreaker.configs.relaxed.wait-duration-in-open-state=10s

# 严格熔断器配置（用于非关键服务）
resilience4j.circuitbreaker.configs.strict.sliding-window-size=50
resilience4j.circuitbreaker.configs.strict.minimum-number-of-calls=5
resilience4j.circuitbreaker.configs.strict.failure-rate-threshold=30
resilience4j.circuitbreaker.configs.strict.wait-duration-in-open-state=60s

# ============================
# 2. 具体实例配置 (Instances)
# ============================

# 用户服务熔断器（使用默认配置）
resilience4j.circuitbreaker.instances.userService.base-config=default

# 订单服务熔断器（使用宽松配置）
resilience4j.circuitbreaker.instances.orderService.base-config=relaxed
resilience4j.circuitbreaker.instances.orderService.wait-duration-in-open-state=20s

# 支付服务熔断器（使用严格配置 + 自定义参数）
resilience4j.circuitbreaker.instances.paymentService.base-config=strict
resilience4j.circuitbreaker.instances.paymentService.sliding-window-type=TIME_BASED
resilience4j.circuitbreaker.instances.paymentService.sliding-window-size=60

# 外部API熔断器（完全自定义配置）
resilience4j.circuitbreaker.instances.externalApi.sliding-window-size=100
resilience4j.circuitbreaker.instances.externalApi.minimum-number-of-calls=10
resilience4j.circuitbreaker.instances.externalApi.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.externalApi.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.externalApi.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.instances.externalApi.automatic-transition-from-open-to-half-open-enabled=true
resilience4j.circuitbreaker.instances.externalApi.slow-call-rate-threshold=50
resilience4j.circuitbreaker.instances.externalApi.slow-call-duration-threshold=3s

# ============================
# 3. 限流器配置
# ============================

# 默认限流器配置
resilience4j.ratelimiter.configs.default.limit-for-period=10
resilience4j.ratelimiter.configs.default.limit-refresh-period=1s
resilience4j.ratelimiter.configs.default.timeout-duration=5s

# 高流量限流配置
resilience4j.ratelimiter.configs.high-traffic.limit-for-period=1000
resilience4j.ratelimiter.configs.high-traffic.limit-refresh-period=1s
resilience4j.ratelimiter.configs.high-traffic.timeout-duration=10s

# 移动端限流器
resilience4j.ratelimiter.instances.mobile.limit-for-period=100
resilience4j.ratelimiter.instances.mobile.limit-refresh-period=1s
resilience4j.ratelimiter.instances.mobile.timeout-duration=5s

# Web端限流器
resilience4j.ratelimiter.instances.web.limit-for-period=50
resilience4j.ratelimiter.instances.web.limit-refresh-period=1s
resilience4j.ratelimiter.instances.web.timeout-duration=5s

# Admin限流器（使用高流量配置）
resilience4j.ratelimiter.instances.admin.base-config=high-traffic

# ============================
# 4. 舱壁隔离配置
# ============================

# 默认舱壁配置
resilience4j.bulkhead.configs.default.max-concurrent-calls=10
resilience4j.bulkhead.configs.default.max-wait-duration=5s

# 数据库连接舱壁
resilience4j.bulkhead.instances.database.max-concurrent-calls=20
resilience4j.bulkhead.instances.database.max-wait-duration=10s

# 外部服务舱壁
resilience4j.bulkhead.instances.externalService.max-concurrent-calls=5
resilience4j.bulkhead.instances.externalService.max-wait-duration=3s

# ============================
# 5. 超时控制配置
# ============================

# 默认超时配置
resilience4j.timelimiter.configs.default.timeout-duration=5s
resilience4j.timelimiter.configs.default.cancel-running-future=true

# 快速超时配置
resilience4j.timelimiter.configs.fast.timeout-duration=1s
resilience4j.timelimiter.configs.fast.cancel-running-future=true

# 慢速超时配置
resilience4j.timelimiter.configs.slow.timeout-duration=30s
resilience4j.timelimiter.configs.slow.cancel-running-future=false

# API调用超时（使用快速配置）
resilience4j.timelimiter.instances.apiCall.base-config=fast

# 报表生成超时（使用慢速配置）
resilience4j.timelimiter.instances.reportGeneration.base-config=slow
```

### 方式二：application.yml (推荐)

```yaml
# ========== Resilience4j 配置方式示例 (YAML) ==========
resilience4j:
  # ============================
  # 1. 熔断器配置
  # ============================
  circuitbreaker:
    # 共享配置模板
    configs:
      default:
        sliding-window-size: 100
        minimum-number-of-calls: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 3s
        sliding-window-type: COUNT_BASED
        ignore-exceptions:
          - java.lang.IllegalArgumentException
        record-exceptions:
          - java.lang.RuntimeException
          - java.io.IOException

      # 宽松配置（用于核心服务）
      relaxed:
        sliding-window-size: 200
        minimum-number-of-calls: 20
        failure-rate-threshold: 60
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 5

      # 严格配置（用于非关键服务）
      strict:
        sliding-window-size: 50
        minimum-number-of-calls: 5
        failure-rate-threshold: 30
        wait-duration-in-open-state: 60s
        permitted-number-of-calls-in-half-open-state: 2

    # 具体实例配置
    instances:
      # 用户服务（继承默认配置）
      userService:
        base-config: default

      # 订单服务（继承宽松配置 + 自定义）
      orderService:
        base-config: relaxed
        wait-duration-in-open-state: 20s
        register-health-indicator: true

      # 支付服务（完全自定义）
      paymentService:
        sliding-window-size: 150
        sliding-window-type: TIME_BASED
        minimum-number-of-calls: 15
        failure-rate-threshold: 40
        wait-duration-in-open-state: 15s
        permitted-number-of-calls-in-half-open-state: 4
        automatic-transition-from-open-to-half-open-enabled: true
        slow-call-rate-threshold: 40
        slow-call-duration-threshold: 2s
        record-exception-predicate: com.example.demo.utils.RecordExceptionPredicate

      # 外部API（继承默认配置）
      externalApi:
        base-config: default

  # ============================
  # 2. 限流器配置
  # ============================
  ratelimiter:
    configs:
      default:
        limit-for-period: 10
        limit-refresh-period: 1s
        timeout-duration: 5s

      # 高流量配置
      high-traffic:
        limit-for-period: 1000
        limit-refresh-period: 1s
        timeout-duration: 10s

      # 移动端配置
      mobile-limiter:
        limit-for-period: 100
        limit-refresh-period: 1s
        timeout-duration: 5s

      # Web端配置
      web-limiter:
        limit-for-period: 50
        limit-refresh-period: 1s
        timeout-duration: 5s

    instances:
      # 移动端限流
      mobile:
        base-config: mobile-limiter
        register-health-indicator: true

      # Web端限流
      web:
        base-config: web-limiter
        register-health-indicator: true

      # Admin限流
      admin:
        base-config: high-traffic
        register-health-indicator: true

      # API限流（默认配置）
      api:
        base-config: default

  # ============================
  # 3. 舱壁隔离配置
  # ============================
  bulkhead:
    configs:
      default:
        max-concurrent-calls: 10
        max-wait-duration: 5s

      # 数据库配置
      db-config:
        max-concurrent-calls: 20
        max-wait-duration: 10s

    instances:
      # 数据库访问舱壁
      database:
        base-config: db-config
        register-health-indicator: true

      # 外部服务舱壁
      externalService:
        max-concurrent-calls: 5
        max-wait-duration: 3s
        writable-stack-trace-enabled: true

  # ============================
  # 4. 超时控制配置
  # ============================
  timelimiter:
    configs:
      default:
        timeout-duration: 5s
        cancel-running-future: true

      # 快速超时
      fast:
        timeout-duration: 1s
        cancel-running-future: true

      # 慢速超时
      slow:
        timeout-duration: 30s
        cancel-running-future: false

    instances:
      # API调用超时
      apiCall:
        base-config: fast

      # 报表生成超时
      reportGeneration:
        base-config: slow
        register-health-indicator: true

  # ============================
  # 5. 重试配置 (Retry)
  # ============================
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - java.lang.RuntimeException
          - java.io.IOException
        ignore-exceptions:
          - java.lang.IllegalArgumentException

      # 积极重试
      aggressive:
        max-attempts: 5
        wait-duration: 500ms
        exponential-backoff-multiplier: 2

    instances:
      # 外部API重试
      externalApiRetry:
        base-config: default
        register-health-indicator: true

      # 数据库重试（积极策略）
      databaseRetry:
        base-config: aggressive
```

## 在代码中使用配置

### 方式一：使用注解（推荐）

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Service
public class UserExternalService {

    // 使用配置文件中定义的 userService 熔断器
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(Long id) {
        // 调用外部服务
        return externalApiClient.getUser(id);
    }

    // 使用配置文件中定义的 mobile 限流器
    @RateLimiter(name = "mobile", fallbackMethod = "getRateLimitFallback")
    public List<User> getUsers() {
        return externalApiClient.getUsers();
    }

    // 使用配置文件中定义的 database 舱壁
    @Bulkhead(name = "database", fallbackMethod = "getBulkheadFallback")
    public List<User> queryUsers(String sql) {
        return jdbcTemplate.query(sql, userRowMapper);
    }

    // 使用配置文件中定义的 apiCall 超时控制
    @TimeLimiter(name = "apiCall", fallbackMethod = "getTimeoutFallback")
    public CompletableFuture<User> getUserAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> externalApiClient.getUser(id));
    }

    // 组合使用多个注解
    @CircuitBreaker(name = "userService")
    @RateLimiter(name = "mobile")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "apiCall")
    public User getUserWithAllProtections(Long id) {
        return externalApiClient.getUser(id);
    }

    // Fallback 方法
    private User getUserFallback(Long id, Exception ex) {
        log.warn("Fallback triggered for user: {}", id, ex);
        return User.getDefaultUser();
    }

    private List<User> getRateLimitFallback(Exception ex) {
        log.warn("Rate limit fallback", ex);
        return Collections.emptyList();
    }

    private List<User> getBulkheadFallback(Exception ex) {
        log.warn("Bulkhead fallback", ex);
        return Collections.emptyList();
    }

    private User getTimeoutFallback(Long id, Exception ex) {
        log.warn("Timeout fallback for user: {}", id, ex);
        return User.getDefaultUser();
    }
}
```

### 方式二：程序化使用（不推荐，已淘汰）

```java
// ❌ 不推荐：编程方式（已在项目中淘汰）
@Service
@Deprecated
public class OldCircuitBreakerService {

    private final CircuitBreakerRegistry registry;

    // 这种方式已被配置方式取代
    public String oldWay() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(100)
                .failureRateThreshold(50)
                .build();
        CircuitBreaker circuitBreaker = registry.circuitBreaker("oldApi", config);

        return circuitBreaker.executeSupplier(() -> externalApi.call());
    }
}
```

## X-Caller 差异化限流的配置方式

X-Caller 限流是通过自定义注解和切面实现的，配置方式略有不同：

```java
// 使用 @CallerRateLimiter 注解
@Service
public class CallerRateLimiterService {

    // 配置方式1: 使用注解参数
    @CallerRateLimiter(
        prefix = "apiLimiter",
        defaultLimitForPeriod = 10,
        callerConfigs = "mobile=100,1,5;web=50,1,5;admin=1000,1,10"
    )
    public String callWithLimit(String data) {
        return service.process(data);
    }

    // 配置方式2: 只声明注解，使用默认配置
    @CallerRateLimiter
    public String callWithDefaultLimit(String data) {
        return service.process(data);
    }
}
```

## 配置优先级

```
实例配置 > 共享配置 (base-config) > 默认配置 (default)
```

例如：

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50    # 默认失败率 50%
      relaxed:
        failure-rate-threshold: 60    # 宽松失败率 60%
    instances:
      orderService:
        base-config: relaxed          # 继承 relaxed 配置
        failure-rate-threshold: 70    # 但这里覆盖为 70%（最高优先级）
```

## 健康检查集成

配置中的 `register-health-indicator: true` 会自动将熔断器状态集成到 Spring Boot Actuator 健康检查：

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        base-config: default
        register-health-indicator: true  # 启用健康检查
```

访问健康检查：
```bash
curl http://localhost:8080/actuator/health
```

响应：
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "userService": {
          "status": "UP",
          "state": "CLOSED"
        }
      }
    }
  }
}
```

## 动态刷新配置

结合 Spring Cloud Config 或 Consul Config，可以实现配置的动态刷新：

```java
@RestController
@RefreshScope  // 支持配置刷新
public class ConfigurableController {

    @CircuitBreaker(name = "dynamicService")
    @RateLimiter(name = "dynamicRateLimiter")
    public String dynamicCall() {
        return service.call();
    }
}
```

刷新配置：
```bash
curl -X POST http://localhost:8080/actuator/refresh
```

## 配置迁移指南

### 从编程方式迁移到配置方式

**迁移前（编程方式）**：
```java
@Configuration
public class OldConfig {
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(100)
                .failureRateThreshold(50)
                .build();
    }
}

@Service
public class MyService {
    private final CircuitBreaker circuitBreaker;

    public String call() {
        return circuitBreaker.executeSupplier(() -> api.call());
    }
}
```

**迁移后（配置方式）**：
```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      myService:
        sliding-window-size: 100
        failure-rate-threshold: 50
```

```java
@Service
public class MyService {
    @CircuitBreaker(name = "myService")
    public String call() {
        return api.call();  // 简洁多了！
    }
}
```

## 总结

| 特性 | 配置方式 | 编程方式 |
|------|----------|----------|
| 推荐度 | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| 维护性 | 高 | 低 |
| 灵活性 | 中 | 高 |
| Spring Boot 集成 | 完美 | 一般 |
| 动态刷新 | 支持 | 需自实现 |

**推荐使用配置方式！** 编程方式仅在特殊场景（如需要动态计算配置参数）时使用。
