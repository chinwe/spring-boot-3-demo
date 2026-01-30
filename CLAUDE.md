# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目信息

这是一个基于 **Spring Boot 3.5.9** 和 **Java 25** 的演示项目，展示了现代 Spring Boot 应用的各种特性和最佳实践。

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
mvn test -Dtest=JooqIntegrationTest
mvn test -Dtest=OrikaMappingServiceTest

# 虚拟线程模块测试
mvn test -Dtest=VirtualThreadServiceTest
mvn test -Dtest=PinDetectionServiceTest
mvn test -Dtest=ScopeValueServiceTest
mvn test -Dtest=StructuredConcurrencyServiceTest
mvn test -Dtest=VirtualThreadMetricsServiceTest
mvn test -Dtest=VirtualThreadControllerTest
mvn test -Dtest=VirtualThreadIntegrationTest
mvn test -Dtest=VirtualThreadPerformanceTest

# 运行特定包下的测试
mvn test -Dtest=com.example.demo.controller.*
mvn test -Dtest=com.example.demo.service.jooq.*

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
com.example.demo/
├── DemoApplication.java                 # 主启动类
│
├── controller/                          # 控制器层 - 处理 HTTP 请求
│   ├── AsyncController.java            # 异步处理相关接口
│   ├── RetryController.java            # 重试相关接口
│   ├── TestController.java             # 基础测试接口
│   ├── jooq/                          # JOOQ 控制器
│   │   └── JooqController.java        # JOOQ 电商系统接口
│   └── orika/                         # Orika 映射控制器
│       └── OrikaController.java        # Orika 对象映射演示
│
├── service/                            # 服务层 - 业务逻辑
│   ├── AsyncService.java               # 异步业务逻辑
│   ├── RetryService.java               # 重试业务逻辑
│   ├── AsyncMetricsService.java        # 异步指标收集
│   ├── jooq/                          # JOOQ 服务
│   │   ├── JooqUserService.java       # 用户服务
│   │   ├── JooqProductService.java     # 商品服务
│   │   ├── JooqOrderService.java      # 订单服务
│   │   └── JooqTransactionService.java # 事务管理服务
│   └── orika/                         # Orika 映射服务
│       └── OrikaMappingService.java     # Orika 映射实现
│
├── mapper/                             # 数据映射层 - MapStruct 映射器
│   ├── UserMapper.java                 # 用户对象映射
│   ├── OrderMapper.java                # 订单对象映射
│   ├── OrderItemMapper.java            # 订单项对象映射
│   ├── AddressMapper.java              # 地址对象映射
│   └── orika/                          # Orika 映射器
│       ├── OrikaUserMapper.java        # 用户 Orika 映射器
│       ├── OrikaOrderMapper.java       # 订单 Orika 映射器
│       └── OrikaAddressMapper.java     # 地址 Orika 映射器
│
├── repository/                         # 数据访问层
│   └── jooq/                          # JOOQ 仓库
│       ├── JooqUserRepository.java     # 用户仓库
│       ├── JooqProductRepository.java  # 商品仓库
│       └── JooqOrderRepository.java   # 订单仓库
│
├── dto/                                # 数据传输对象 - 用于 API 交互
│   ├── UserDto.java                    # 用户 DTO
│   ├── OrderDto.java                   # 订单 DTO
│   ├── OrderItemDto.java               # 订单项 DTO
│   ├── AddressDto.java                 # 地址 DTO
│   ├── AsyncTaskDto.java              # 异步任务 DTO
│   ├── AsyncErrorResponse.java         # 异步错误响应 DTO
│   └── jooq/                          # JOOQ DTO
│       ├── JooqUserDto.java            # 用户 DTO
│       ├── JooqProductDto.java          # 商品 DTO
│       ├── JooqOrderDto.java           # 订单 DTO
│       ├── JooqOrderItemDto.java       # 订单项 DTO
│       ├── JooqCreateOrderRequest.java  # 创建订单请求
│       ├── JooqCreateProductRequest.java # 创建商品请求
│       └── JooqOrderQueryRequest.java   # 订单查询请求
│
├── vo/                                 # 值对象 - 用于视图展示
│   ├── AsyncTaskVo.java               # 异步任务 VO
│   └── DelayVo.java                    # 延迟 VO
│
├── entity/                            # 实体类 - 领域模型
│   ├── User.java                       # 用户实体
│   ├── Order.java                      # 订单实体
│   ├── OrderItem.java                  # 订单项实体
│   ├── Address.java                    # 地址实体
│   └── Customer.java                   # 客户实体
│
├── configuration/                      # 配置类
│   ├── AsyncConfiguration.java         # 异步线程池配置
│   ├── RetryConfiguration.java         # Spring Retry 配置
│   └── DemoRetryConfiguration.java    # 演示配置
│
├── annotation/                         # 自定义注解
│   ├── LocalRetryable.java             # 本地服务重试注解
│   └── RemoteRetryable.java            # 远程服务重试注解
│
├── exception/                         # 异常类
│   ├── TemporaryException.java        # 临时异常（可重试）
│   ├── NetworkException.java          # 网络异常（适合重试）
│   ├── BusinessException.java         # 业务异常（不可重试）
│   ├── AsyncExceptionHandler.java      # 异步异常处理器
│   ├── JooqExceptionHandler.java      # JOOQ 异常处理器
│   └── virtual/                       # 虚拟线程异常
│       ├── PinDetectedException.java  # Pin 检测异常
│       └── VirtualThreadException.java # 虚拟线程异常
│
└── listener/                          # 监听器
    └── CustomRetryListener.java        # 自定义重试监听器
```

### 虚拟线程模块结构

```
com.example.demo.virtual/
├── configuration/                      # 虚拟线程配置
│   ├── VirtualThreadConfiguration.java     # 虚拟线程执行器配置
│   └── PinDetectionConfiguration.java      # Pin 检测配置（JFR）
│
├── context/                            # 上下文
│   └── UserContext.java                  # ScopedValue 用户上下文
│
├── controller/                         # 虚拟线程控制器
│   └── VirtualThreadController.java      # 虚拟线程 API 接口
│
├── dto/                                # 数据传输对象
│   ├── VirtualThreadTaskDto.java         # 虚拟线程任务 DTO
│   ├── PinDetectionReport.java           # Pin 检测报告
│   ├── PerformanceComparisonReport.java  # 性能对比报告
│   └── StructuredConcurrencyResult.java  # 结构化并发结果
│
├── service/                            # 服务层
│   ├── VirtualThreadService.java         # 虚拟线程基础服务
│   ├── PinDetectionService.java          # Pin 检测服务
│   ├── ScopeValueService.java            # ScopedValue 演示服务
│   ├── StructuredConcurrencyService.java # 结构化并发服务
│   └── VirtualThreadMetricsService.java  # 虚拟线程指标服务
│
└── vo/                                 # 值对象
    ├── VirtualThreadTaskVo.java          # 虚拟线程任务 VO
    └── PinDetectionVo.java               # Pin 检测 VO
```

### 核心功能模块

#### 1. Spring Retry 模块
- **位置**: `service.RetryService`, `controller.RetryController`
- **功能**: 展示声明式和编程式重试的各种场景
- **特点**:
  - 自定义 `@LocalRetryable` 和 `@RemoteRetryable` 注解
  - 支持条件重试、SpEL 表达式重试
  - 自定义 `RetryListener` 监听重试过程
  - 区分临时异常（可重试）和业务异常（不可重试）

#### 2. 异步处理模块
- **位置**: `service.AsyncService`, `controller.AsyncController`
- **功能**: 异步任务执行和回调
- **配置**: `AsyncConfiguration` 配置异步线程池
  - 核心线程数: 5
  - 最大线程数: 20
  - 队列容量: 100
  - 线程存活时间: 60s
- **支持方式**: CompletableFuture、DeferredResult、Callable

#### 3. 对象映射模块
- **位置**: `mapper/` 和 `mapper/orika/` 包
- **工具**: MapStruct 1.6.3 和 Orika
- **特点**:
  - **MapStruct**: 编译时生成映射代码，零反射开销
  - **Orika**: 运行时映射，支持复杂转换器
  - 重要: MapStruct 与 Lombok 集成需要在 `maven-compiler-plugin` 中正确配置注解处理器顺序

#### 4. JOOQ 模块
- **位置**: `repository/jooq/`, `service/jooq/`, `controller/jooq/`
- **功能**: 完整的电商系统演示
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

#### 5. 虚拟线程模块（Java 25）
- **位置**: `virtual/` 包
- **功能**: 展示 Java 25 虚拟线程、Pin 检测、ScopedValue 和结构化并发
- **特点**:
  - **虚拟线程**: 使用 `Executors.newVirtualThreadPerTaskExecutor()` 创建轻量级线程
  - **Pin 检测**: 检测虚拟线程被固定到载体线程的场景（synchronized、本地方法、文件 I/O）
  - **ScopedValue**: 不可变的线程上下文传递机制，自动清理
  - **结构化并发**: 使用 CompletableFuture 演示结构化并发模式
- **配置**:
  - JFR 录制用于 Pin 检测（通过 `-Djfr.enabled=true` 启用）
  - 虚拟线程执行器 Bean 配置
  - 需要启用预览功能：`--enable-preview`

## 技术栈要点

### 核心依赖
```xml
<!-- Spring Boot -->
<spring.boot.version>3.5.9</spring.boot.version>

<!-- 数据库访问 -->
<jooq.version>3.19.15</jooq.version>

<!-- 对象映射 -->
<mapstruct.version>1.6.3</mapstruct.version>
<orika.version>1.5.4</orika.version>

<!-- API 文档 -->
<springdoc.version>2.8.0</springdoc.version>

<!-- 测试框架 -->
<junit-jupiter.version>5.11.4</junit-jupiter.version>
<testcontainers.version>1.20.4</testcontainers.version>
```

### Java 25 特性
- 项目使用 Java 25，可以使用最新的 Java 特性

**虚拟线程相关 API**:
```java
// 创建虚拟线程
Thread vThread = Thread.ofVirtual().start(() -> { /* 任务代码 */ });

// 使用执行器
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// 判断是否为虚拟线程
boolean isVirtual = Thread.currentThread().isVirtual();

// ScopedValue 使用
ScopedValue.where(CONTEXT, "value").call(() -> {
    String value = CONTEXT.get();
    return value;
});

// StructuredTaskScope 使用
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Future<String> f1 = scope.fork(() -> task1());
    Future<String> f2 = scope.fork(() -> task2());
    scope.join();
    return f1.resultNow() + f2.resultNow();
}
```

**启动应用时可选的 JVM 参数**:
```bash
# 启用 JFR Pin 检测
java -XX:StartFlightRecording=filename=recording.jfr,duration=60s -Djfr.enabled=true -jar demo.jar
```

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

### API 接口列表

#### 异步处理接口 (`/async`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/async/completable-future/{taskName}` | CompletableFuture 示例 |
| POST | `/async/deferred-result` | DeferredResult 示例 |
| GET | `/async/callable/{delaySeconds}` | Callable 示例 |
| GET | `/async/concurrent-test` | 并发请求示例 |
| GET | `/async/metrics` | 获取异步指标 |
| POST | `/async/metrics/reset` | 重置指标 |

#### 重试机制接口 (`/retry`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/retry/basic` | 基本重试示例 |
| GET | `/retry/local` | 本地服务重试 |
| GET | `/retry/remote` | 远程服务重试 |
| GET | `/retry/conditional` | 条件重试 |
| GET | `/retry/imperative` | 编程式重试 |
| GET | `/retry/spel` | SpEL 表达式重试 |
| GET | `/retry/all-examples` | 执行所有示例 |

#### JOOQ 电商接口 (`/api/jooq`)

**用户管理**:
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/jooq/users` | 创建用户 |
| GET | `/api/jooq/users/{id}` | 查询用户 |
| GET | `/api/jooq/users` | 查询所有用户 |
| GET | `/api/jooq/users/username/{username}` | 按用户名查询 |

**商品管理**:
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/jooq/products` | 创建商品 |
| POST | `/api/jooq/products/batch` | 批量创建商品 |
| GET | `/api/jooq/products/{id}` | 查询商品 |
| GET | `/api/jooq/products` | 按分类查询（支持分页） |
| GET | `/api/jooq/products/low-stock` | 查询低库存商品 |
| GET | `/api/jooq/products/stock/{category}` | 查询分类库存统计 |

**订单管理**:
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/jooq/orders` | 创建订单（事务） |
| GET | `/api/jooq/orders/{id}` | 查询订单详情 |
| GET | `/api/jooq/users/{userId}/orders` | 查询用户订单 |
| GET | `/api/jooq/orders/statistics` | 订单统计 |

#### Orika 映射接口 (`/orika`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/orika/user` | 基础对象映射 |
| GET | `/orika/order` | 复杂对象映射 |
| GET | `/orika/orders` | 集合映射 |
| POST | `/orika/batch` | 批量映射 |

#### 测试接口 (`/test`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/test/hello` | 基础测试接口 |
| GET | `/test/echo/{message}` | 回显消息 |

#### 虚拟线程接口 (`/api/virtual`)

**基础虚拟线程**:
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/virtual/basic-task` | 执行基础虚拟线程任务 |
| GET | `/api/virtual/batch-tasks` | 批量执行虚拟线程任务 |
| GET | `/api/virtual/simulate-pinning` | 模拟 Pin 场景（synchronized） |

**Pin 检测**:
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/virtual/pin-detection` | 检测 Pin 线程事件 |
| POST | `/api/virtual/pin-test` | 测试 Pin 场景（SYNCHRONIZED/NATIVE/FILE_IO） |

**ScopedValue**:
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/virtual/scoped-value` | 演示 ScopedValue 用法 |
| GET | `/api/virtual/scoped-value-comparison` | 对比 ThreadLocal 与 ScopedValue |

**结构化并发**:
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/virtual/structured-concurrency` | 执行结构化并发任务 |
| GET | `/api/virtual/structured-concurrency/shutdown-on-success` | 演示 ShutdownOnSuccess 模式 |
| GET | `/api/virtual/structured-concurrency/error-handling` | 演示错误处理 |

**性能测试**:
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/virtual/performance-comparison` | 性能对比（传统线程池 vs 虚拟线程） |
| GET | `/api/virtual/demo-all` | 综合演示所有功能 |

## 数据库设计

### JOOQ 电商表结构

```sql
-- 用户表
CREATE TABLE j_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE j_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE j_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES j_users(id)
);

-- 订单项表
CREATE TABLE j_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES j_orders(id),
    FOREIGN KEY (product_id) REFERENCES j_products(id)
);
```

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

### 数据库配置
- H2 内存数据库
- 自动执行 schema.sql 初始化脚本

## 测试策略

### 单元测试
- 使用 Mockito 进行依赖模拟
- 覆盖控制器、服务、映射器等各层

### 集成测试
- 使用 Testcontainers 进行容器化测试
- JOOQ 集成测试验证数据库操作
- 异步集成测试验证并发处理能力

### 性能测试
- 异步性能测试 (`AsyncPerformanceTest`)
- 对象映射性能对比测试
- 虚拟线程性能测试 (`VirtualThreadPerformanceTest`)
  - 对比传统线程池和虚拟线程在不同规模任务下的性能
  - 测试吞吐量、内存使用、延迟等指标

### 虚拟线程测试
- **VirtualThreadServiceTest**: 测试虚拟线程基础功能
- **PinDetectionServiceTest**: 测试 Pin 检测功能
- **ScopeValueServiceTest**: 测试 ScopedValue 上下文传递
- **StructuredConcurrencyServiceTest**: 测试结构化并发
- **VirtualThreadMetricsServiceTest**: 测试性能指标收集
- **VirtualThreadIntegrationTest**: 端到端集成测试
- **VirtualThreadPerformanceTest**: 性能对比测试

## 测试文件结构

```
src/test/java/com/example/demo/
├── AsyncIntegrationTest.java          # 异步集成测试
├── AsyncPerformanceTest.java          # 异步性能测试
├── TestContainerExampleTest.java      # Testcontainers 示例测试
├── TestControllerTest.java            # 测试控制器单元测试
├── TestControllerMockitoTest.java     # 测试控制器 Mockito 测试
├── controller/
│   ├── AsyncControllerTest.java       # 异步控制器测试
│   ├── RetryControllerTest.java       # 重试控制器测试
│   ├── jooq/
│   │   └── JooqControllerTest.java    # JOOQ 控制器测试
│   └── orika/
│       └── OrikaControllerTest.java   # Orika 控制器测试
├── service/
│   ├── AsyncServiceTest.java          # 异步服务测试
│   ├── RetryServiceTest.java          # 重试服务测试
│   ├── jooq/
│   │   └── JooqIntegrationTest.java   # JOOQ 集成测试
│   └── orika/
│       └── OrikaMappingServiceTest.java # Orika 映射服务测试
├── mapper/
│   └── OrderMapperTest.java           # 订单映射器测试
└── exception/
    └── AsyncExceptionHandlerTest.java # 异常处理器测试
├── virtual/                            # 虚拟线程测试
│   ├── VirtualThreadIntegrationTest.java  # 虚拟线程集成测试
│   ├── VirtualThreadPerformanceTest.java  # 虚拟线程性能测试
│   ├── controller/
│   │   └── VirtualThreadControllerTest.java # 虚拟线程控制器测试
│   └── service/
│       ├── VirtualThreadServiceTest.java    # 虚拟线程服务测试
│       ├── PinDetectionServiceTest.java     # Pin 检测服务测试
│       ├── ScopeValueServiceTest.java       # ScopeValue 服务测试
│       ├── StructuredConcurrencyServiceTest.java # 结构化并发服务测试
│       └── VirtualThreadMetricsServiceTest.java  # 虚拟线程指标服务测试
```
