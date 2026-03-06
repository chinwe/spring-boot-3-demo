## Context

当前项目是一个 Spring Boot 3.5.9 演示项目，展示了异步处理、重试机制、JOOQ、虚拟线程、Sentinel 等多种技术特性。项目使用 H2 内存数据库进行开发和测试。虽然 Actuator 依赖已经包含在项目中，但尚未启用 Prometheus 指标端点。

### 当前状态
- Spring Boot 3.5.9 已包含 Micrometer 内置支持
- `spring-boot-starter-actuator` 依赖已存在（需要确认）
- 无自定义业务指标
- 无 Prometheus 端点暴露

### 约束
- 需保持与现有模块的兼容性
- 端点应在生产环境中仅内部访问
- 指标收集不应影响业务性能

## Goals / Non-Goals

**Goals:**
- 暴露 Prometheus 兼容的指标端点
- 为关键业务流程添加自定义指标（异步任务、重试次数、数据库查询等）
- 提供集成测试验证指标正确性

**Non-Goals:**
- 不集成 Grafana 仪表板（留待后续）
- 不实现告警规则（由监控系统配置）
- 不添加分布式链路追踪（可考虑后续集成 OpenTelemetry）

## Decisions

### 1. 指标注册表选择
**选择**: Micrometer Prometheus Registry

**理由**:
- Spring Boot Actuator 原生支持 Micrometer
- Prometheus 是 CNCF 毕业项目，生态成熟
- 与现有的虚拟线程和异步处理模型兼容

**替代方案**: Influx、Graphite - 考虑但未选择，因 Prometheus 在云原生环境更通用

### 2. 指标分类
**选择**: 三层指标体系

| 层级 | 指标类型 | 示例 |
|------|----------|------|
| JVM | 堆内存、GC、线程 | `jvm_memory_used`, `jvm_gc_pause` |
| HTTP | 请求计数、延迟 | `http_server_requests` |
| 业务 | 异步任务、重试、DB 查询 | `async_tasks_total`, `retry_attempts` |

**理由**: 分层设计便于问题定位和性能分析

### 3. 自定义指标实现方式
**选择**: 使用 `MeterRegistry` 创建 `Counter`、`Gauge`、`Timer`

**理由**:
- Micrometer 提供类型安全的 API
- 支持标签（Tag）进行多维分析
- 与 Spring 生命周期集成良好

### 4. 端点安全
**选择**: 在 `application.yml` 中配置端点暴露，生产环境通过网络策略限制访问

**理由**:
- Spring Boot 3.x 默认不暴露所有 Actuator 端点
- 简化开发流程（开发环境无需认证）
- 生产安全由基础设施层保证

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 指标收集影响性能 | 使用 Micrometer 的无锁计数器，异步写入 |
| 高基数标签导致内存膨胀 | 限制标签值枚举，避免使用用户 ID 等高基数标签 |
| 端点暴露敏感信息 | 生产环境配置 IP 白名单或使用 Spring Security |
| Prometheus 抓取超时 | 设置合理的 `scrape_interval` 和 `scrape_timeout` |

## Migration Plan

1. **开发阶段**:
   - 添加依赖配置
   - 创建自定义指标类
   - 编写集成测试

2. **测试阶段**:
   - 验证端点可访问性
   - 确认指标数据准确性
   - 性能基准测试

3. **部署阶段**:
   - 滚动更新，无破坏性变更
   - 配置 Prometheus 抓取目标
   - 验证 Grafana 仪表板

## Open Questions

1. **Q**: 是否需要为虚拟线程单独创建指标？
   **A**: 需要，虚拟线程指标（如 pinned 线程数）对诊断性能问题很重要

2. **Q**: Sentinel 自带指标如何与 Micrometer 集成？
   **A**: Sentinel 有 Micrometer 适配器，后续可考虑集成

3. **Q**: 是否需要记录 SQL 慢查询指标？
   **A**: 暂不通过 Micrometer 记录，可由数据库或 JOOQ 自带统计提供
