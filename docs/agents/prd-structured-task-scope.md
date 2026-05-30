# PRD: StructuredTaskScope 真实 API 演示模块

## Problem Statement

当前项目的结构化并发演示使用 `CompletableFuture` 模拟 `StructuredTaskScope` 的行为，但存在语义错误：

- `executeShutdownOnSuccess` 使用 `anyOf()` 但不会取消其他任务，slow task 仍然跑完
- `demonstrateErrorHandling` 使用 `allOf()` 等所有任务完成，不会在第一个失败时取消其他任务
- ExecutorService 每次方法调用都创建新实例但从未关闭，存在资源泄漏

作为技术展示 Demo，项目有责任展示正确的 JDK API 用法。用 CompletableFuture 模拟无法体现结构化并发的核心价值——"一个失败/成功就取消其他"。

## Solution

新增一个基于 JDK `StructuredTaskScope` 真实 API 的演示模块，使用**订单详情聚合**这一真实业务场景，展示三种并发策略：

1. **ShutdownOnFailure** — 任一子任务失败就取消其他（"快失败"）
2. **ShutdownOnSuccess** — 任一子任务成功就取消其他（"快返回"）
3. **自定义 Scope** — 更复杂的策略

同时保留现有的 CompletableFuture 模拟实现，作为对比参考。

## User Stories

### 核心功能

1. 作为开发者，我想要看到 `ShutdownOnFailure` 的真实行为演示，以便理解"任一失败即取消全部"的语义
2. 作为开发者，我想要看到 `ShutdownOnSuccess` 的真实行为演示，以便理解"竞速返回"的语义
3. 作为开发者，我想要看到自定义 `StructuredTaskScope` 子类的实现，以便学习如何实现复杂策略
4. 作为开发者，我想要对比 CompletableFuture 模拟与真实 StructuredTaskScope 的行为差异，以便理解两者本质区别
5. 作为开发者，我想要看到子任务被取消后的生命周期管理（中断、资源清理），以便理解结构化并发的安全保证

### 业务场景 — 订单详情聚合

6. 作为用户，我想要并行获取用户信息、订单项列表和支付状态来组装完整订单详情，以便减少总等待时间
7. 作为用户，当支付状态查询失败时，我希望整个订单详情聚合立刻失败（而不是等所有子任务完成），以便快速得到错误反馈
8. 作为用户，当多个数据源都能返回结果时，我希望获取最快的那个（竞速模式），以便最小化等待时间
9. 作为用户，当部分非关键数据源失败时，我希望仍能获得基本订单信息（降级模式），以便系统具备容错能力

### API 接口

10. 作为 API 调用者，我想要通过 `POST /api/virtual/sts/shutdown-on-failure` 触发 ShutdownOnFailure 演示，以便观察快失败行为
11. 作为 API 调用者，我想要通过 `POST /api/virtual/sts/shutdown-on-success` 触发 ShutdownOnSuccess 演示，以便观察竞速返回行为
12. 作为 API 调用者，我想要通过 `POST /api/virtual/sts/custom-scope` 触发自定义策略演示，以便观察复杂聚合行为
13. 作为 API 调用者，我想要通过 `GET /api/virtual/sts/compare` 同时执行 CompletableFuture 版和 StructuredTaskScope 版，以便对比两者的执行时间和行为差异
14. 作为 API 调用者，我想要通过 `GET /api/virtual/sts/demo-all` 一次性运行所有演示，以便快速查看全部功能

### 模拟与控制

15. 作为开发者，我想要控制模拟支付 API 的失败率（0%-100%），以便测试不同失败场景下的行为
16. 作为开发者，我想要控制各数据源的响应延迟，以便观察不同时序下的取消行为
17. 作为开发者，我想要在响应中看到每个子任务的状态（成功/失败/取消）和执行时间，以便分析并发行为

### 可观测性

18. 作为开发者，我想要在日志中看到子任务的启动、完成和取消事件，以便追踪结构化并发的生命周期
19. 作为开发者，我想要在响应中看到被取消的子任务列表，以便验证取消行为是否正确

## Implementation Decisions

### 模块划分

**新增 Service — `StructuredTaskScopeService`**
- 独立于现有 `StructuredConcurrencyService`（保留不动）
- 负责所有真实的 `StructuredTaskScope` API 调用
- 使用现有 JOOQ 仓库（`JooqUserRepository`、`JooqOrderRepository`）获取真实数据
- 新增模拟支付 API 的内部方法（sleep + 可控失败率）

**新增 Controller — `StructuredTaskScopeController`**
- 端点前缀 `/api/virtual/sts/`
- 注入 `StructuredTaskScopeService` 和现有 `StructuredConcurrencyService`（用于对比端点）

**新增 DTO — `OrderAggregationResult`**
- 包含三个子结果：用户信息、订单项列表、支付状态
- 每个子结果有独立的状态（SUCCESS/FAILED/CANCELLED）和耗时
- 包含整体策略、总耗时、是否成功

### 三种策略的具体实现

**ShutdownOnFailure — 订单详情聚合**
- 三个并行子任务：查用户、查订单项、查支付状态
- 任一失败 → 其他立即取消
- 全部成功 → 聚合为完整订单详情

**ShutdownOnSuccess — 竞速获取支付状态**
- 模拟向多个支付网关查询状态（3 个子任务，不同延迟）
- 第一个成功返回 → 取消其他
- 全部失败 → 返回失败

**自定义 Scope — 降级聚合**
- 继承 `StructuredTaskScope` 实现自定义策略
- 核心数据源（用户信息、订单项）必须成功
- 非核心数据源（支付状态）失败不影响整体结果
- 提供基本订单信息 + 降级标记

### 模拟支付 API 设计

- 通过 Service 内部方法实现，不依赖外部系统
- 可配置参数：延迟毫秒数、失败概率（0.0-1.0）
- 返回结构化的支付状态信息（状态、交易号、时间戳）
- 通过请求参数动态控制，不需要重启应用

### 现有代码变更

- 不修改现有的 `StructuredConcurrencyService` 和 `VirtualThreadController`
- 仅在 `virtual/` 包下新增文件

## Testing Decisions

### 测试原则

- 测试外部行为（API 响应内容），不测试内部实现细节
- 验证子任务的取消行为是否正确（通过响应中的状态判断）
- 验证不同策略下的聚合结果是否符合预期

### 测试的模块

1. **StructuredTaskScopeService** — 单元测试，验证三种策略的业务逻辑
2. **StructuredTaskScopeController** — 单元测试，验证 API 端点的请求/响应

### 测试策略

- **Service 测试**：使用 `@MockitoBean` 模拟 JOOQ 仓库和外部依赖
  - 优先参考：`VirtualThreadServiceTest`、`ScopeValueServiceTest`
  - 测试场景：
    - 所有子任务成功 → 聚合成功
    - 关键子任务失败 → ShutdownOnFailure 立即取消
    - 竞速模式 → ShutdownOnSuccess 返回最快结果
    - 降级模式 → 非关键失败仍返回基本结果
- **Controller 测试**：使用 `MockMvc` + `@MockitoBean`
  - 优先参考：`VirtualThreadControllerTest`
  - 验证 HTTP 状态码和响应结构

## Out of Scope

- 不修改现有的 `StructuredConcurrencyService` 或 `VirtualThreadController`
- 不新建数据库表（使用现有 JOOQ 表 + 模拟 API）
- 不集成 GraalVM Native Image
- 不处理 StructuredTaskScope 的预览 API 兼容性问题（项目已配置 `--enable-preview`）
- 不实现 StructuredTaskScope 与 Spring 事务管理的集成
- 不实现前端 UI

## Further Notes

### JDK 版本兼容性

`StructuredTaskScope` 在 JDK 21-23 中作为 Preview API 存在，API 形态经历了多次变化。JDK 24+ 已将其作为标准 API。本项目使用 JDK 25，`--enable-preview` 已在 Maven Surefire 中配置，确保兼容性。

### CompletableFuture vs StructuredTaskScope 核心差异

| 特性 | CompletableFuture | StructuredTaskScope |
|------|-------------------|---------------------|
| 取消传播 | 手动，容易遗漏 | 自动，框架保证 |
| 异常处理 | 需要 exceptionally/handle | join() 时统一抛出 |
| 线程泄漏 | 可能（未取消的任务继续运行） | 不可能（scope 关闭时全部清理） |
| 可观测性 | 弱（无内置支持） | 强（与 JFR 集成） |
| 代码可读性 | 链式调用，嵌套复杂 | try-with-resources，结构清晰 |
