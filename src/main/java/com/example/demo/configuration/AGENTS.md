# Configuration Package - 配置管理

**生成时间**: 2026-01-18

---

## OVERVIEW

异步处理与 Spring Retry 框架配置，包含线程池管理、重试策略及 SpEL 表达式支持。

---

## 去哪里找

| 任务 | 位置 | 说明 |
|------|------|------|
| 异步执行器 | `AsyncConfiguration` | @EnableAsync, ThreadPoolTaskExecutor (core=5, max=20, queue=100) |
| 编程式重试 | `RetryConfiguration` | @EnableRetry, RetryTemplate, CustomRetryListener |
| SpEL 重试条件 | `DemoRetryConfiguration` | 运行时配置 Bean（RuntimeConfigs）、异常检查器 |
| 重试监听 | `listener/CustomRetryListener` | Emoji 日志（🚀 启动、✅ 成功、❌ 失败、🔄 最终失败、🎯 完成） |

---

## 约定（偏离标准）

### 重试策略配置

- **RetryTemplate**: 最大重试 3 次，仅对 `TemporaryException` 和 `NetworkException` 重试
- **退避策略**: 指数退避，初始 1s，乘数 2.0，最大 10s
- **异步超时**: MVC 异步支持默认 30 秒超时

### SpEL 表达式示例

可用 Bean 引用于 `@Retryable` 注解：
- `@retryable(maxAttempts = "@runtimeConfigs.maxAttempts")` - 动态最大重试次数
- `@retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root.exception)")` - 自定义异常判断

---

## 禁止模式（本项目）

1. ❌ **不混用依赖注入** - RetryConfiguration 使用 `@Autowired` 注入监听器，应使用 `@Resource`
2. ❌ **不硬编码重试参数** - 优先使用 SpEL 表达式引用配置 Bean
