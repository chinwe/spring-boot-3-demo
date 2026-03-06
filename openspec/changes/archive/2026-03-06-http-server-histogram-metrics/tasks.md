## 1. 配置变更

- [x] 1.1 在 `application.properties` 中启用直方图配置
- [x] 1.2 验证现有百分位数配置保持不变（0.5, 0.95, 0.99）

## 2. 测试实现

- [x] 2.1 在 `MetricsIntegrationTest` 中添加直方图指标验证测试
- [x] 2.2 添加测试验证 `http_server_requests_seconds_bucket` 指标存在
- [x] 2.3 添加测试验证 `_count` 和 `_sum` 指标存在
- [x] 2.4 添加测试验证 bucket 数据格式（包含 `le` 标签）
- [x] 2.5 添加测试验证现有百分位数指标仍然可用

## 3. 本地验证

- [x] 3.1 启动应用并访问 `/actuator/prometheus` 端点
- [x] 3.2 确认响应中包含 `http_server_requests_seconds_bucket` 指标
- [x] 3.3 确认 bucket 数量和范围符合预期（至少 20 个，覆盖 1ms-30s）
- [x] 3.4 运行 `MetricsIntegrationTest` 确保所有测试通过

## 4. 文档更新

- [x] 4.1 更新项目 README 中的 Metrics 模块说明
- [x] 4.2 添加 Prometheus 查询示例到文档

## 5. 完成确认

- [x] 5.1 运行完整测试套件：`mvn test`
- [x] 5.2 确认无回归问题（MetricsIntegrationTest 11/11 通过，虚拟线程测试的 2 个错误为预存问题）
