# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目信息

这是一个基于 **Spring Boot 3.5.9** 和 **Java 21** 的演示项目，展示了现代 Spring Boot 应用的各种特性和最佳实践。

## 常用命令

### 构建和运行
```bash
# 清理并编译项目
mvn clean compile

# 打包项目
mvn clean package

# 运行应用程序
mvn spring-boot:run

# 跳过测试快速构建
mvn clean install -DskipTests
```

### 测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=RetryServiceTest
mvn test -Dtest=AsyncControllerTest
mvn test -Dtest=OrderMapperTest

# 运行特定包下的测试
mvn test -Dtest=com.example.demo.controller.*

# 运行集成测试
mvn test -Dtest=AsyncIntegrationTest

# 清理并运行所有测试
mvn clean test
```

### 编译特定内容
```bash
# 编译测试代码
mvn test-compile

# 编译主代码
mvn compile
```

## 代码架构

### 分层架构
项目遵循经典的分层架构模式：

```
controller/     # 控制器层 - 处理 HTTP 请求
    - AsyncController      # 异步处理相关接口
    - RetryController      # 重试相关接口
    - TestController       # 基础测试接口

service/        # 服务层 - 业务逻辑
    - AsyncService         # 异步业务逻辑
    - RetryService         # 重试业务逻辑
    - AsyncMetricsService  # 异步指标收集

mapper/         # 数据映射层 - MapStruct 映射器
    - UserMapper           # 用户对象映射
    - OrderMapper          # 订单对象映射
    - OrderItemMapper      # 订单项对象映射
    - AddressMapper        # 地址对象映射

dto/            # 数据传输对象 - 用于 API 交互
vo/             # 值对象 - 用于视图展示
entity/         # 实体类 - 领域模型

configuration/  # 配置类
    - RetryConfiguration      # Spring Retry 配置
    - AsyncConfiguration      # 异步线程池配置
    - DemoRetryConfiguration  # 演示配置

annotation/     # 自定义注解
    - @LocalRetryable         # 本地服务重试注解
    - @RemoteRetryable        # 远程服务重试注解
```

### 核心功能模块

#### 1. Spring Retry 模块
- **位置**: `service/RetryService`, `controller/RetryController`
- **功能**: 展示声明式和编程式重试的各种场景
- **特点**:
  - 自定义 `@LocalRetryable` 和 `@RemoteRetryable` 注解
  - 支持条件重试、SpEL 表达式重试
  - 自定义 `RetryListener` 监听重试过程
  - 区分临时异常（可重试）和业务异常（不可重试）

#### 2. 异步处理模块
- **位置**: `service/AsyncService`, `controller/AsyncController`
- **功能**: 异步任务执行和回调
- **配置**: `AsyncConfiguration` 配置异步线程池
  - 核心线程数: 5
  - 最大线程数: 20
  - 队列容量: 100
  - 线程存活时间: 60s

#### 3. 对象映射模块
- **位置**: `mapper/` 包
- **工具**: MapStruct 1.6.3
- **特点**: 编译时生成映射代码，零反射开销
- **重要**: MapStruct 与 Lombok 集成需要在 `maven-compiler-plugin` 中正确配置注解处理器顺序

#### 4. JOOQ 模块
- **位置**: `repository/jooq/`, `service/jooq/`, `controller/jooq/`
- **功能**: 展示 JOOQ 的各种功能
- **特点**:
  - **CRUD 操作**: 基础的增删改查
  - **复杂查询**: JOIN、聚合、分组统计
  - **事务管理**: 声明式事务处理
  - **高级特性**: 批量操作、Upsert、分页
- **表设计**:
  - `j_users`: 用户表
  - `j_products`: 商品表
  - `j_orders`: 订单表
  - `j_order_items`: 订单项表

## 技术栈要点

### Java 21 特性
- 项目使用 Java 21，可以使用最新的 Java 特性（如虚拟线程、模式匹配等）

### MapStruct + Lombok 集成
```xml
<!-- 在 pom.xml 中，Lombok 必须在 MapStruct 之前处理 -->
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
    </path>
</annotationProcessorPaths>
```

### 异常处理设计
- `TemporaryException`: 临时异常，应该重试
- `NetworkException`: 网络异常，适合重试
- `BusinessException`: 业务异常，不应该重试

### 数据库
- **H2 内存数据库**: 用于开发和测试
- **JOOQ**: 类型安全的数据库访问
- **Redis**: 缓存支持（已配置）

## API 文档

项目集成了 SpringDoc OpenAPI (Swagger UI)：
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 代码规范

1. **代码和日志**: 使用英文
2. **注释**: 使用中文
3. **包结构**: 严格按照分层架构组织代码
4. **命名规范**: 遵循 Java 标准命名约定

## 重要配置

### 异步配置
- 请求超时: 30 秒
- 线程名前缀: `async-task-`

### 服务器配置
- 默认端口: 8080

## 测试策略

- **单元测试**: 使用 Mockito 进行依赖模拟
- **集成测试**: 使用 Testcontainers 进行容器化测试
- **性能测试**: 包含异步性能测试
