# Spring Cloud Circuit Breaker (Resilience4j) 熔断器模块

## 概述

本模块基于 **Resilience4j 2.3.0** 实现，为项目提供完整的容错机制，包括熔断、限流、舱壁隔离和超时控制。核心特色是支持基于 HTTP Header `X-Caller` 的业务维度差异化限流功能。

## 配置方式说明

**Resilience4j 支持两种配置方式：**

| 方式 | 推荐度 | 说明 |
|------|--------|------|
| **配置文件** | ⭐⭐⭐⭐⭐ | 在 `application.yml` 中配置，通过 `@CircuitBreaker` 等注解引用 |
| **编程方式** | ⭐⭐ | 在 Java 配置类中创建（本项目已实现，用于演示） |

> 💡 **推荐使用配置文件方式！** 详见：[配置方式详细指南](./circuitbreaker-configuration-guide.md) | [配置示例文件](../src/main/resources/application-resilience4j.yml.example)

### 配置方式示例

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        sliding-window-size: 100
        failure-rate-threshold: 50
```

```java
@Service
class UserService {
    @CircuitBreaker(name = "userService")
    public User getUser(Long id) {
        return externalApi.getUser(id);
    }
}
```

### 编程方式示例（本项目实现）

```java
@Configuration
class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreakerRegistry registry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(100)
                .failureRateThreshold(50)
                .build();
        return CircuitBreakerRegistry.of(config);
    }
}
```

## 技术栈

- **Resilience4j**: 2.3.0
- **Spring Boot**: 3.5.9
- **Java**: 25
- **Springdoc OpenAPI**: 2.8.0

## 模块结构

```
com.example.demo.circuitbreaker/
├── controller/             # 控制器层
│   └── CircuitBreakerController.java       # REST API 接口
│
├── service/                # 服务层
│   ├── CircuitBreakerService.java          # 熔断器核心服务（使用注解）
│   ├── CallerRateLimiterService.java       # X-Caller 限流服务
│   ├── ExternalApiService.java             # 外部服务模拟
│   └── CircuitBreakerMetricsService.java   # 指标收集服务
│
├── annotation/             # 自定义注解
│   └── CallerRateLimiter.java              # X-Caller 限流注解
│
├── aspect/                 # 切面
│   └── CallerRateLimiterAspect.java        # X-Caller 限流切面（核心功能）
│
├── dto/                    # 数据传输对象
│   ├── CircuitBreakerResultDto.java        # 熔断器执行结果
│   ├── ExternalApiRequestDto.java          # 外部API请求
│   ├── CircuitBreakerStateDto.java         # 熔断器状态
│   ├── RateLimitExceededDto.java           # 限流超出响应
│   └── MetricsDto.java                     # 指标数据
│
├── vo/                     # 值对象
│   ├── CircuitBreakerStateVo.java          # 熔断器状态视图
│   └── MetricsVo.java                      # 指标视图
│
├── model/                  # 模型类
│   └── CallerRateLimit.java                # 调用方限流配置
│
└── exception/              # 异常类
    ├── CircuitBreakerOpenException.java    # 熔断器打开异常
    ├── RateLimitExceededException.java     # 限流超出异常
    ├── BulkheadFullException.java          # 舱壁已满异常
    ├── TimeOutExceededException.java       # 超时异常
    └── CircuitBreakerExceptionHandler.java  # 统一异常处理器
```

> **注**: 配置类已删除，改用配置文件方式（`application.properties`）

## 核心功能

### 1. 熔断器 (Circuit Breaker)

保护服务免受级联故障影响。当失败率超过阈值时，熔断器打开，快速失败而不是让请求堆积。

**配置参数:**
- 滑动窗口大小: 100
- 最小调用次数: 10
- 失败率阈值: 50%
- 等待时长: 30秒
- 半开状态调用数: 3次

**状态转换:**
```
CLOSED (关闭) → OPEN (打开) → HALF_OPEN (半开) → CLOSED
```

### 2. 限流器 (Rate Limiter)

限制单位时间内的请求数，防止系统过载。

**配置参数:**
- 限流周期: 1秒
- 周期内请求数: 10个
- 超时等待: 5秒

### 3. 舱壁隔离 (Bulkhead)

限制并发请求数，隔离资源使用。

**配置参数:**
- 最大并发数: 10
- 最大等待时间: 5秒

### 4. 超时控制 (Time Limiter)

防止长时间运行的请求阻塞系统。

**配置参数:**
- 超时时长: 5秒
- 自动取消: true

### 5. X-Caller 差异化限流 ⭐

根据 HTTP Header `X-Caller` 为不同调用方设置不同的限流配额。

**使用示例:**

```java
@CallerRateLimiter(
    prefix = "callerLimiter",
    defaultLimitForPeriod = 10,
    callerConfigs = "mobile=100,1,5;web=50,1,5;admin=1000,1,10"
)
public String myMethod() {
    // 方法实现
}
```

**配置格式:** `callerName=limitForPeriod,limitRefreshPeriodInSeconds,timeoutDurationInSeconds`

**支持的调用方:**
- `mobile`: 100 请求/秒
- `web`: 50 请求/秒
- `admin`: 1000 请求/秒
- `其他`: 10 请求/秒（默认）

## API 接口

### 熔断器相关接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/circuitbreaker/circuit-breaker` | 熔断器示例 |
| GET | `/api/circuitbreaker/state/{name}` | 获取熔断器状态 |
| GET | `/api/circuitbreaker/state/all` | 获取所有状态 |
| POST | `/api/circuitbreaker/reset/{name}` | 重置熔断器 |

### 限流器相关接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/circuitbreaker/rate-limiter` | 限流器示例 |
| GET | `/api/circuitbreaker/rate-limit/basic` | X-Caller 基础限流 |
| GET | `/api/circuitbreaker/rate-limit/caller-specific` | X-Caller 差异化限流 |
| GET | `/api/circuitbreaker/rate-limit/with-param` | 参数限流 |

### 其他容错模式接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/circuitbreaker/bulkhead` | 舱壁隔离示例 |
| POST | `/api/circuitbreaker/time-limiter` | 超时控制示例 |
| POST | `/api/circuitbreaker/all-resilience` | 组合所有模式 |

### 监控和管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/circuitbreaker/metrics` | 获取指标 |
| POST | `/api/circuitbreaker/metrics/reset` | 重置指标 |
| GET | `/api/circuitbreaker/circuit-breakers` | 获取所有熔断器名称 |
| GET | `/api/circuitbreaker/demo-all` | 综合演示 |

## 使用示例

### 1. 使用熔断器

```bash
curl -X POST http://localhost:8080/api/circuitbreaker/circuit-breaker \
  -H "Content-Type: application/json" \
  -d '{"endpoint":"/api/users","simulateFailure":false}'
```

### 2. 使用 X-Caller 差异化限流

```bash
# mobile 客户端: 100 请求/秒
curl -X GET http://localhost:8080/api/circuitbreaker/rate-limit/caller-specific \
  -H "X-Caller: mobile"

# web 客户端: 50 请求/秒
curl -X GET http://localhost:8080/api/circuitbreaker/rate-limit/caller-specific \
  -H "X-Caller: web"

# admin 客户端: 1000 请求/秒
curl -X GET http://localhost:8080/api/circuitbreaker/rate-limit/caller-specific \
  -H "X-Caller: admin"
```

### 3. 查看熔断器状态

```bash
# 查看特定熔断器状态
curl http://localhost:8080/api/circuitbreaker/state/externalApi

# 查看所有熔断器状态
curl http://localhost:8080/api/circuitbreaker/state/all
```

### 4. 查看指标

```bash
curl http://localhost:8080/api/circuitbreaker/metrics
```

## 配置文件

在 `application.properties` 中添加以下配置:

```properties
# ========== Resilience4j Configuration ==========

# Circuit Breaker Configuration
resilience4j.circuitbreaker.configs.default.sliding-window-size=100
resilience4j.circuitbreaker.configs.default.minimum-number-of-calls=10
resilience4j.circuitbreaker.configs.default.failure-rate-threshold=50
resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.configs.default.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.configs.default.automatic-transition-from-open-to-half-open-enabled=true
resilience4j.circuitbreaker.configs.default.slow-call-rate-threshold=50
resilience4j.circuitbreaker.configs.default.slow-call-duration-threshold=3s

# Rate Limiter Configuration
resilience4j.ratelimiter.configs.default.limit-for-period=10
resilience4j.ratelimiter.configs.default.limit-refresh-period=1s
resilience4j.ratelimiter.configs.default.timeout-duration=5s

# Bulkhead Configuration
resilience4j.bulkhead.configs.default.max-concurrent-calls=10
resilience4j.bulkhead.configs.default.max-wait-duration=5s

# Time Limiter Configuration
resilience4j.timelimiter.configs.default.timeout-duration=5s
resilience4j.timelimiter.configs.default.cancel-running-future=true

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,circuitbreakers,ratelimiters,bulkheads
management.endpoint.health.show-details=always
```

## 依赖项

```xml
<properties>
    <resilience4j.version>2.3.0</resilience4j.version>
</properties>

<dependencies>
    <!-- Resilience4j Spring Boot 3 -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
        <version>${resilience4j.version}</version>
    </dependency>

    <!-- Resilience4j 全部模块 -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-all</artifactId>
        <version>${resilience4j.version}</version>
    </dependency>

    <!-- Micrometer 指标监控 -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-micrometer</artifactId>
        <version>${resilience4j.version}</version>
    </dependency>

    <!-- Actuator 监控端点 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Cache 用于限流状态缓存 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
</dependencies>
```

## 测试

### 运行测试

```bash
# 运行熔断器模块测试
mvn test -Dtest=CircuitBreakerServiceTest
mvn test -Dtest=CircuitBreakerControllerTest
mvn test -Dtest=CallerRateLimiterServiceTest
```

### 测试覆盖

- **CircuitBreakerServiceTest**: 9 个测试用例
- **CircuitBreakerControllerTest**: 17 个测试用例
- **CallerRateLimiterServiceTest**: 12 个测试用例

## 与现有模块集成

### Spring Retry 集成

Resilience4j 可以与 Spring Retry 的 `@Retryable` 注解组合使用：

```java
@Retryable(maxAttempts = 3)
@CircuitBreaker(name = "myCircuitBreaker")
public String resilientMethod() {
    // 重试在内层处理，熔断在外层处理
}
```

### 异步模块集成

支持 CompletableFuture 异步场景：

```java
@Async
@CircuitBreaker(name = "asyncCircuitBreaker")
public CompletableFuture<String> asyncMethod() {
    return CompletableFuture.completedFuture("result");
}
```

## 监控端点

通过 Spring Boot Actuator 获取监控数据：

```bash
# 熔断器状态
curl http://localhost:8080/actuator/circuitbreakers

# 限流器状态
curl http://localhost:8080/actuator/ratelimiters

# 舱壁状态
curl http://localhost:8080/actuator/bulkheads

# 健康检查
curl http://localhost:8080/actuator/health
```

## Swagger UI

访问 `http://localhost:8080/swagger-ui.html` 查看 API 文档和进行接口测试。

## 最佳实践

1. **合理设置阈值**: 根据实际业务场景设置熔断器和限流器的阈值
2. **监控指标**: 定期查看熔断器状态和限流指标，及时调整配置
3. **优雅降级**: 结合 `@Fallback` 方法实现降级逻辑
4. **差异化限流**: 为不同调用方设置合理的限流配额
5. **组合使用**: 根据需要组合多种容错模式

## 故障排查

### 常见问题

1. **熔断器一直打开**: 检查失败率是否过高，或手动重置熔断器
2. **限流不生效**: 确认 `X-Caller` Header 是否正确设置
3. **指标不准确**: 检查 Actuator 配置是否正确

### 日志级别

```properties
# 启用 Resilience4j 调试日志
logging.io.github.resilience4j=DEBUG
logging.com.example.demo.circuitbreaker=DEBUG
```

## 参考文档

- [Resilience4j 官方文档](https://resilience4j.readme.io/)
- [Spring Boot 3.x 文档](https://docs.spring.io/spring-boot/docs/3.5.9/reference/html/)
- [项目 CLAUDE.md](../../CLAUDE.md)
