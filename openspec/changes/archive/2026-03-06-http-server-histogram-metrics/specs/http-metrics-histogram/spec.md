# HTTP Metrics Histogram

提供 HTTP 请求延迟的直方图统计指标，支持在 Prometheus 端进行灵活的聚合和百分位计算。

## ADDED Requirements

### Requirement: 启用 HTTP 请求直方图指标

系统 SHALL 为 `http_server_requests` 指标启用直方图（histogram）发布功能，生成按延迟分段的 bucket 指标。

#### Scenario: Prometheus 端点暴露直方图指标
- **WHEN** 访问 `/actuator/prometheus` 端点
- **THEN** 响应包含 `http_server_requests_seconds_bucket` 指标系列
- **AND** 每个 bucket 包含 `le` (less than or equal) 标签表示延迟上限
- **AND** 包含 `_count` 和 `_sum` 后缀指标用于速率计算

#### Scenario: 直方图 bucket 覆盖合理范围
- **WHEN** 查询 `http_server_requests_seconds_bucket` 指标
- **THEN** bucket 边界从 1ms 开始，覆盖至少到 30s
- **AND** bucket 数量不少于 20 个
- **AND** bucket 间隔呈指数分布（更适合延迟分布特征）

### Requirement: 保留现有百分位数配置

系统 SHALL 在启用直方图的同时，保留现有的百分位数（0.5, 0.95, 0.99）配置，确保向后兼容。

#### Scenario: 百分位数指标继续可用
- **WHEN** 访问 `/actuator/prometheus` 端点
- **THEN** `http_server_requests_seconds_max` 指标存在
- **AND** `http_server_requests_seconds{quantile="0.5"}` 指标存在
- **AND** `http_server_requests_seconds{quantile="0.95"}` 指标存在
- **AND** `http_server_requests_seconds{quantile="0.99"}` 指标存在

### Requirement: 支持标签维度

直方图指标 SHALL 继承 `http_server_requests` 的所有标签维度，支持按标签分组聚合。

#### Scenario: 按标签聚合查询
- **WHEN** 使用 Prometheus 查询 `sum(rate(http_server_requests_seconds_bucket[5m])) by (uri, le)`
- **THEN** 查询结果按 URI 分组返回延迟分布
- **AND** 可按 `method`, `status`, `exception` 等标签分组

### Requirement: 测试验证

系统 SHALL 包含集成测试验证直方图指标的正确性和可用性。

#### Scenario: 集成测试验证直方图存在
- **GIVEN** 应用已启动
- **WHEN** 执行集成测试请求 `/actuator/prometheus`
- **THEN** 响应包含 `http_server_requests_seconds_bucket` 指标
- **AND** 响应包含 `http_server_requests_seconds_count` 指标
- **AND** 响应包含 `http_server_requests_seconds_sum` 指标

#### Scenario: 集成测试验证 bucket 数据格式
- **GIVEN** Prometheus 端点响应内容
- **WHEN** 解析 `http_server_requests_seconds_bucket` 指标行
- **THEN** 每行包含 `le` 标签（如 `le="0.001"`, `le="0.003"`）
- **AND** 数值为该 bucket 的累计请求数
