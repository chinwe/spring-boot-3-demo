## Why

Spring Boot 3 项目目前缺乏可观测性基础设施。生产环境应用需要实时监控 JVM 指标、HTTP 请求统计、自定义业务指标等，以便及时发现和诊断问题。Micrometer 是 Java 应用监控的事实标准，集成后可以将指标暴露给 Prometheus、Grafana 等监控系统。

## What Changes

- **新增 Micrometer 依赖**: 添加 `micrometer-registry-prometheus` 依赖
- **配置 Prometheus 端点**: 在 `application.yml` 中配置 Actuator 和 Prometheus 相关配置
- **自定义业务指标**: 为现有模块（异步处理、重试机制、JOOQ、虚拟线程、Sentinel）添加关键业务指标
- **集成测试**: 验证端点可访问性和指标数据正确性

## Capabilities

### New Capabilities
- `metrics-collection`: 收集和暴露 JVM、HTTP 和自定义业务指标
- `prometheus-endpoint`: 提供 `/actuator/prometheus` 端点供 Prometheus 抓取指标

### Modified Capabilities
- 无

## Impact

- **依赖**: 添加 `micrometer-registry-prometheus` (已包含在 spring-boot-starter-actuator 中)
- **配置**: 修改 `application.yml` 和 `application-dev.yml`
- **新增代码**:
  - `com.example.demo.metrics` 包 - 自定义指标配置和收集器
- **新增测试**:
  - `MetricsIntegrationTest` - 验证端点和指标
- **API 端点**: 暴露 `/actuator/prometheus` 端点（默认需认证或仅内部访问）
- **兼容性**: 无破坏性变更，向后兼容
