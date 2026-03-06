## Context

当前项目的 `http_server_requests` 指标通过 Micrometer 集成收集，在 `application.properties` 中已配置：
- 百分位数: 0.5, 0.95, 0.99
- SLA 阈值: 50ms, 100ms, 200ms, 500ms, 1s, 2s

但未启用直方图（histogram）发布功能。直方图是 Prometheus 观察延迟分布的标准方式，能提供更灵活的聚合查询能力。

**技术约束**:
- Spring Boot 3.5.9 + Micrometer (已集成)
- Prometheus 作为监控后端
- 配置优先原则，无需代码修改

**利益相关者**:
- 运维团队：需要更精确的延迟分布数据
- 开发团队：需要快速定位性能问题

## Goals / Non-Goals

**Goals:**
- 启用 `http_server_requests` 的直方图发布
- 确保直方图数据在 Prometheus 端点正确暴露
- 添加测试验证直方图指标的存在和格式

**Non-Goals:**
- 修改现有百分位数配置（保留 0.5, 0.95, 0.99）
- 自定义直方图 bucket 边界（使用 Micrometer 默认配置）
- 实现自定义 MeterBinder（使用 Spring Boot 自动配置）

## Decisions

### 1. 启用方式选择

**决策**: 通过 `application.properties` 配置启用，而非编程方式

**理由**:
- Spring Boot Actuator 已内置 Micrometer 集成，配置文件是标准方式
- 无需额外 Java 代码，降低维护成本
- 支持不同环境使用不同配置

**替代方案**: 编程方式配置 `MeterRegistryCustomizer`
- **放弃原因**: 过度设计，配置文件已足够

### 2. Bucket 配置

**决策**: 使用 Micrometer 默认 bucket 边界

**理由**:
- Micrometer 提供的默认 bucket 是经过优化的（从 1ms 到 30s，指数增长）
- 无需手动维护 bucket 列表
- 大多数场景下表现良好

**默认 bucket**: 1ms, 3ms, 5ms, 7ms, 10ms, 15ms, 20ms, 25ms, 30ms, 40ms, 50ms, 75ms, 100ms, 150ms, 200ms, 300ms, 400ms, 500ms, 750ms, 1s, 1.5s, 2s, 3s, 4s, 5s, 7.5s, 10s, 15s, 20s, 30s

**替代方案**: 自定义 bucket
- **放弃原因**: 增加复杂度，仅在有明显性能问题或业务需求时才需要

### 3. 指标基数考虑

**决策**: 保持当前 URI 粒度，暂不实施 URI 模板化

**理由**:
- 演示项目规模较小，基数风险可控
- Spring Boot 2.x+ 的 WebMvcMetricsFilter 已做 URI 模板化处理

**生产环境建议**: 如果应用有大量动态 URI（如 `/api/users/{id}`），考虑配置：
```properties
management.metrics.tags.uri.pattern=/api/users/*
```

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| **指标基数爆炸** | Prometheus 存储压力 | 监控 `http_server_requests_seconds_bucket` 的基数，必要时实施 URI 模板化 |
| **性能开销** | 每次请求记录多个 bucket | Micrometer 高效实现，开销通常 < 1%，可忽略 |
| **Prometheus 查询复杂度** | 需要学习 `histogram_quantile()` | 提供常用查询示例文档 |
| **回滚困难** | 配置变更需重启 | 配置优先，可随时禁用 |

## Migration Plan

### 部署步骤

1. **修改配置** (`application.properties`)
   ```properties
   management.metrics.distribution.percentiles-histogram.http.server.requests=true
   ```

2. **重启应用** (Spring Boot 需要重启加载配置)

3. **验证** - 访问 `/actuator/prometheus` 确认包含 `http_server_requests_seconds_bucket` 指标

4. **更新 Prometheus 查询** - 开始使用 `histogram_quantile()` 进行百分位计算

### 回滚策略

如遇问题，移除配置项并重启即可回滚。已采集的历史数据不受影响。

## Open Questions

无 - 这是一个简单的配置变更，所有关键决策已明确。

## Appendix: Prometheus 查询示例

启用直方图后，可使用以下查询：

```promql
# 计算 95 百分位延迟
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# 计算每个 URI 的 P95 延迟
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (uri, le))

# 计算请求速率（按 HTTP 状态码分组）
sum(rate(http_server_requests_seconds_count[5m])) by (status)

# 检测慢请求（超过 1s）
rate(http_server_requests_seconds_bucket{le="1.0"}[5m]) / rate(http_server_requests_seconds_count[5m])
```
