## Why

当前 `http_server_requests` 指标虽然配置了百分位数（percentiles）和 SLA 阈值，但缺少完整的直方图（histogram）统计。启用直方图可以：

1. **提供更精确的聚合统计** - Prometheus 可以在服务端计算任意百分位数，而不只是预配置的 50/95/99
2. **支持更灵活的查询** - 可以通过 `histogram_quantile()` 函数动态计算任何百分位值
3. **更好的速率计算** - 直方图配合 `rate()` 或 `irate()` 函数可以准确计算请求速率的分布
4. **符合最佳实践** - Micrometer 官方推荐为高流量端点启用直方图

## What Changes

- 启用 `http_server_requests` 指标的直方图发布功能（`publishPercentileHistogram`）
- 优化 HTTP 指标的百分位数配置，保留关键的 50/95/99 用于快速查询
- 添加测试验证直方图指标正确暴露

## Capabilities

### New Capabilities
- `http-metrics-histogram`: HTTP 请求直方图指标支持，提供可聚合的请求延迟分布统计

### Modified Capabilities
无（仅增强现有指标配置，不改变业务行为）

## Impact

- **配置文件**: `application.properties` - 添加 `management.metrics.distribution.percentiles-histogram.http.server.requests=true`
- **Actuator 端点**: `/actuator/prometheus` 将包含额外的 `http_server_requests_seconds_bucket` 指标
- **Prometheus 查询**: 可使用 `histogram_quantile()` 进行更灵活的百分位计算
- **性能影响**: 直方图会产生额外的指标系列（bucket 数量 × URI 数量），对于高 URI 数量的应用需注意基数控制
