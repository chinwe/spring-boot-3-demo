# Spring Retry 示例文档

本项目包含了完整的Spring Retry功能示例，展示了声明式和编程式重试的各种使用场景。

## 🚀 功能特性

### 1. 依赖配置
- Spring Retry 核心库
- Spring Boot AOP 支持
- 完整的Maven配置

### 2. 核心组件

#### 配置类
- `RetryConfiguration`: 启用Spring Retry并配置RetryTemplate
- `DemoRetryConfiguration`: 演示配置，提供SpEL表达式所需的Bean

#### 服务类
- `RetryService`: 核心重试服务，包含各种重试场景的示例

#### 控制器
- `RetryController`: 提供HTTP接口测试各种重试功能

#### 异常类
- `TemporaryException`: 临时异常，通常需要重试
- `NetworkException`: 网络异常，适合重试
- `BusinessException`: 业务异常，不应该重试

#### 自定义注解
- `@LocalRetryable`: 本地服务重试注解（保守策略）
- `@RemoteRetryable`: 远程服务重试注解（积极策略）

#### 监听器
- `CustomRetryListener`: 自定义重试监听器，记录重试过程

## 📋 重试场景示例

### 1. 基本声明式重试
```java
@Retryable(retryFor = TemporaryException.class, 
           maxAttempts = 3, 
           backoff = @Backoff(delay = 1000))
public String basicRetryExample(boolean shouldSucceed) {
    // 业务逻辑
}

@Recover
public String recoverFromBasicRetry(TemporaryException ex) {
    // 恢复逻辑
}
```

### 2. 自定义注解重试
```java
@LocalRetryable(retryFor = TemporaryException.class)
public String localServiceCall(boolean shouldSucceed) {
    // 本地服务调用
}

@RemoteRetryable(retryFor = NetworkException.class)
public String remoteServiceCall(boolean shouldSucceed) {
    // 远程服务调用
}
```

### 3. 条件重试
```java
@Retryable(retryFor = {TemporaryException.class, NetworkException.class},
           noRetryFor = BusinessException.class,
           maxAttempts = 3,
           backoff = @Backoff(delay = 500, maxDelay = 5000, multiplier = 2.0))
public String conditionalRetryExample(String exceptionType) {
    // 根据异常类型决定是否重试
}
```

### 4. SpEL表达式重试
```java
@Retryable(maxAttemptsExpression = "args[1] == 'critical' ? 5 : 2",
           retryFor = TemporaryException.class,
           backoff = @Backoff(delayExpression = "#{100}", 
                              maxDelayExpression = "#{5000}",
                              multiplierExpression = "#{2.0}"))
public String spelRetryExample(boolean shouldSucceed, String priority) {
    // 根据优先级动态调整重试次数
}
```

### 5. 编程式重试
```java
public String imperativeRetryExample(boolean shouldSucceed) {
    return retryTemplate.execute(
        // RetryCallback
        (RetryCallback<String, Exception>) context -> {
            // 业务逻辑
        },
        // RecoveryCallback
        (RecoveryCallback<String>) context -> {
            // 恢复逻辑
        }
    );
}
```

## 🔧 配置说明

### RetryTemplate配置
```java
@Bean
public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    
    // 重试策略
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);
    
    // 退避策略
    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(1000L);
    backOffPolicy.setMultiplier(2.0);
    backOffPolicy.setMaxInterval(10000L);
    
    retryTemplate.setRetryPolicy(retryPolicy);
    retryTemplate.setBackOffPolicy(backOffPolicy);
    
    return retryTemplate;
}
```

### 自定义监听器
```java
@Bean
public RetryListener customRetryListener() {
    return new RetryListener() {
        // 重试开始
        public boolean open(RetryContext context, RetryCallback callback) { }
        
        // 重试成功
        public void onSuccess(RetryContext context, RetryCallback callback, Object result) { }
        
        // 重试失败
        public void onError(RetryContext context, RetryCallback callback, Throwable throwable) { }
        
        // 重试结束
        public void close(RetryContext context, RetryCallback callback, Throwable throwable) { }
    };
}
```

## 🌐 API接口

### 基本重试接口
- `GET /retry/basic?shouldSucceed=true` - 基本重试示例
- `GET /retry/local?shouldSucceed=false` - 本地服务重试
- `GET /retry/remote?shouldSucceed=false` - 远程服务重试

### 高级重试接口
- `GET /retry/conditional?exceptionType=temporary` - 条件重试
  - exceptionType: `temporary`, `network`, `business`
- `GET /retry/imperative?shouldSucceed=false` - 编程式重试
- `GET /retry/spel?shouldSucceed=false&priority=critical` - SpEL表达式重试
  - priority: `normal`, `critical`

### 工具接口
- `POST /retry/reset` - 重置计数器
- `GET /retry/all-examples` - 执行所有示例

## 🧪 测试

### 运行单元测试
```bash
mvn test -Dtest=RetryServiceTest
```

### 运行集成测试
```bash
mvn test -Dtest=RetryControllerTest
```

### 运行所有测试
```bash
mvn test
```

## 📊 监控和日志

项目配置了详细的日志记录，可以观察重试过程：

```
🚀 开始重试操作: Lambda Operation
❌ 重试操作失败: Lambda Operation | 第1次尝试 | 异常: TemporaryException | 消息: 模拟临时异常
❌ 重试操作失败: Lambda Operation | 第2次尝试 | 异常: TemporaryException | 消息: 模拟临时异常
✅ 重试操作成功: Lambda Operation | 重试次数: 2 | 总耗时: 2108ms
```

## 🎯 最佳实践

1. **选择合适的重试策略**
   - 本地操作：较少重试次数，较短延迟
   - 远程调用：更多重试次数，指数退避

2. **异常分类**
   - 临时性异常（网络超时、服务暂时不可用）：应该重试
   - 业务异常（参数错误、权限不足）：不应该重试

3. **使用恢复机制**
   - 提供@Recover方法处理最终失败的情况
   - 恢复方法应该返回合理的默认值或执行降级逻辑

4. **监控重试行为**
   - 使用RetryListener监控重试过程
   - 记录重试统计信息，分析重试模式

5. **配置合理的退避策略**
   - 避免过于频繁的重试
   - 使用随机化避免雷群效应

## 🚦 启动和测试

1. 启动应用：
```bash
mvn spring-boot:run
```

2. 访问示例接口：
```bash
curl "http://localhost:8080/retry/basic?shouldSucceed=false"
curl "http://localhost:8080/retry/all-examples"
```

3. 查看日志观察重试过程。