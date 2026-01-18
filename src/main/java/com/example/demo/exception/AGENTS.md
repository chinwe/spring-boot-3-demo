# Exception Package 知识库

**生成时间**: 2026-01-18

---

## 概述

全局异常处理架构，提供分层异常处理、日志记录和标准化错误响应。

---

## 去哪里找

| 任务 | 位置 | 说明 |
|------|------|------|
| JOOQ 业务异常 | `JooqExceptionHandler` | 优先级 1，处理业务特定异常 |
| 异步操作异常 | `AsyncExceptionHandler` | 优先级 2，处理超时和运行时异常 |
| 自定义业务异常 | `BusinessException` | 非重试异常 |
| 可重试异常 | `TemporaryException`, `NetworkException` | 用于 Spring Retry |

---

## 约定（偏离标准）

### 异常处理器优先级

使用 `@Order` 注解控制处理器执行顺序：
```java
@Order(1)  // 优先处理业务异常
@RestControllerAdvice
public class JooqExceptionHandler { }

@Order(2)  // 后处理通用异常
@RestControllerAdvice
public class AsyncExceptionHandler { }
```

### 异常转发模式

检查异常类型后重新抛出，确保正确处理器处理：
```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
    if (ex instanceof JooqExceptionHandler.EntityNotFoundException) {
        throw ex;  // 转发到 JooqExceptionHandler
    }
    // 处理其他运行时异常
}
```

### 异常类型与重试策略

| 异常类型 | 重试 | 用途 |
|---------|------|------|
| `BusinessException` | ❌ 否 | 业务逻辑错误，重试无意义 |
| `TemporaryException` | ✅ 是 | 临时性故障（如服务短暂不可用） |
| `NetworkException` | ✅ 是 | 网络相关故障（如超时、连接失败） |

### 日志模式（强制）

所有异常日志必须包含异常对象：
```java
log.error("Async request timeout occurred", ex);  // ✅ 正确
log.error("Timeout exception occurred: {}", ex.getMessage());  // ❌ 缺少异常对象
```

---

## 禁止模式（本包）

1. ❌ **不吞没异常** - 日志记录后必须抛出或返回错误响应
2. ❌ **不忽略异常转发** - 必须使用 `instanceof` 检查并 `throw ex` 转发
3. ❌ **不使用 `@Autowired`** - 遵循项目规范使用 `@Resource`
