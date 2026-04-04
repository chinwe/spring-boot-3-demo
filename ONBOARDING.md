# 🚀 Spring Boot 3 Demo — 新人快速上手指南

> **技术栈**: Spring Boot 3.5.9 · Java 25 · Maven · H2 · JOOQ · MapStruct · Lombok
>
> **阅读时间**: ~15 分钟

---

## 一、项目简介

这是一个**教学演示型项目**，用 Spring Boot 3.5.9 + Java 25 展示现代 Java Web 应用的核心架构能力。项目包含 **10 大功能模块**，每个模块独立封装、有完整测试，适合逐个学习。

| # | 模块 | 亮点 | 入口路径 |
|---|------|------|----------|
| 1 | **异步处理** | CompletableFuture / DeferredResult / Callable 三种异步模式 | `/async/*` |
| 2 | **Spring Retry** | 声明式 / 编程式 / 条件 / SpEL 重试 | `/retry/*` |
| 3 | **JOOQ 电商** | 完整 CRUD + 事务 + 多表 JOIN | `/api/jooq/*` |
| 4 | **对象映射** | MapStruct + Orika 双引擎对比 | `/orika/*` |
| 5 | **虚拟线程** | ScopedValue / 结构化并发 / Pin 检测 | `/api/virtual/*` |
| 6 | **熔断器 (Resilience4j)** | 熔断 / 限流 / 舱壁隔离 / 超时 / X-Caller 限流 | `/api/circuitbreaker/*` |
| 7 | **Sentinel** | 流控 / 熔断降级 / 热点参数 / 系统自适应保护 | `/api/sentinel/*` |
| 8 | **日志脱敏** | Log4j2 自定义 Layout，7 种脱敏策略 | `/api/logging/test/*` |
| 9 | **指标监控** | Micrometer + Prometheus + Actuator | `/actuator/*` |
| 10 | **链路追踪** | 分布式追踪集成 | `tracing/` |

---

## 二、环境准备

### 2.1 必备工具

| 工具 | 版本要求 | 验证命令 |
|------|---------|---------|
| JDK | **25+** (项目硬性要求) | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Git | 任意 | `git --version` |
| IDE | IntelliJ IDEA（推荐）/ VS Code | — |

> ⚠️ **注意**: `pom.xml` 中配置了本地 JDK 路径 `D:\jdk\jdk-25.0.1+8`，如你的 JDK 安装位置不同，需修改 `maven-compiler-plugin` 的 `<executable>` 和 `maven-surefire-plugin` 的 `<jvm>` 配置。

### 2.2 可选工具

| 工具 | 用途 |
|------|------|
| Redis | 项目引入了 `spring-boot-starter-data-redis`，启动时无 Redis 不影响运行 |
| Docker | 用于 Testcontainers 集成测试 |
| Postman / curl | 调用 API 接口 |

---

## 三、快速启动（3 步）

```bash
# 1️⃣ 克隆项目
git clone <repository-url>
cd spring-boot-3-demo

# 2️⃣ 编译
mvn clean compile          # 首次编译会下载依赖，约 1-3 分钟

# 3️⃣ 启动
mvn spring-boot:run        # 启动成功后访问 http://localhost:8080
```

### 验证启动成功

```bash
# 快速检查
curl http://localhost:8080/test/hello
# → "Hello from TestController!"

# Swagger UI（推荐）
# 浏览器打开 → http://localhost:8080/swagger-ui.html
```

---

## 四、项目结构详解

```
src/main/java/com/example/demo/
│
├── DemoApplication.java          ← 启动类
├── TestController.java           ← 基础测试接口 (/test/*)
│
├── controller/                   ← 【异步 + 重试 + JOOQ + Orika 控制器】
│   ├── AsyncController.java          异步处理 REST API
│   ├── RetryController.java          Spring Retry REST API
│   ├── jooq/JooqController.java      JOOQ 电商 REST API
│   └── orika/OrikaController.java    Orika 映射演示 REST API
│
├── service/                      ← 【业务逻辑层】
│   ├── AsyncService.java             异步任务服务
│   ├── AsyncMetricsService.java      异步指标服务
│   ├── RetryService.java             重试服务
│   ├── jooq/                         JOOQ 业务服务（用户/商品/订单/事务）
│   └── orika/OrikaMappingService.java Orika 映射服务
│
├── repository/jooq/              ← 【数据访问层（仅 JOOQ 使用）】
│   ├── JooqUserRepository.java
│   ├── JooqProductRepository.java
│   └── JooqOrderRepository.java
│
├── mapper/                       ← 【MapStruct 映射器】
│   ├── OrderMapper.java              订单映射（高级映射示例）
│   ├── UserMapper.java               用户映射
│   ├── AddressMapper.java            地址映射
│   ├── OrderItemMapper.java          订单项映射
│   ├── JooqProductMapper.java        JOOQ 商品映射
│   └── orika/                        Orika 映射器实现
│
├── dto/                          ← 【数据传输对象 (入参)】
│   ├── AsyncTaskDto.java
│   ├── OrderDto.java / UserDto.java ...
│   └── jooq/                         JOOQ 专用 DTO
│
├── vo/                           ← 【值对象 (视图 / 出参)】
│   ├── AsyncTaskVo.java
│   └── DelayVo.java
│
├── entity/                       ← 【领域实体】
│   ├── User.java / Order.java / Customer.java / Address.java / OrderItem.java
│
├── configuration/                ← 【配置类】
│   ├── AsyncConfiguration.java       异步线程池配置
│   ├── RetryConfiguration.java       重试模板配置
│   └── DemoRetryConfiguration.java   重试监听器配置
│
├── exception/                    ← 【全局异常处理】
│   ├── JooqExceptionHandler.java     @Order(1) — JOOQ 业务异常
│   ├── AsyncExceptionHandler.java    @Order(2) — 异步异常
│   ├── BusinessException.java        业务异常
│   ├── NetworkException.java         网络异常
│   └── TemporaryException.java       临时异常
│
├── annotation/                   ← 【自定义注解】
│   ├── LocalRetryable.java           本地服务重试注解
│   └── RemoteRetryable.java          远程服务重试注解
│
├── listener/                     ← 【事件监听器】
│   └── CustomRetryListener.java      重试事件监听
│
├── virtual/                      ← 【虚拟线程模块 (Java 25)】
│   ├── controller/                   REST API
│   ├── service/                      虚拟线程 / Pin检测 / ScopedValue / 结构化并发
│   ├── dto/ & vo/                    数据对象
│   ├── exception/                    异常定义
│   └── configuration/                虚拟线程配置
│
├── circuitbreaker/               ← 【Resilience4j 熔断器模块】
│   ├── controller/                   REST API
│   ├── service/                      熔断/限流/舱壁/超时/指标服务
│   ├── annotation/                   @CallerRateLimiter 自定义注解
│   ├── aspect/                       限流切面
│   ├── dto/ & vo/ & model/           数据对象
│   └── exception/                    异常定义 + 异常处理器
│
├── sentinel/                     ← 【Sentinel 流控模块】
│   ├── controller/                   REST API
│   ├── service/                      流控/降级/热点参数/系统保护
│   ├── configuration/                Sentinel 配置
│   ├── dto/ & vo/                    数据对象
│   └── exception/                    异常定义 + 异常处理器
│
├── logging/                      ← 【日志脱敏模块】
│   ├── controller/                   测试 API
│   ├── configuration/                Log4j2 配置
│   ├── desensitize/                  脱敏引擎
│   │   ├── layout/                       自定义 PatternLayout
│   │   ├── model/                         脱敏配置/规则/类型
│   │   └── strategy/                      7 种脱敏策略
│   └── dto/                          测试请求/响应
│
├── metrics/                      ← 【指标监控模块】
│   ├── binder/                       自定义 Metrics（异步/JOOQ/重试/虚拟线程/Sentinel）
│   └── configuration/                Metrics 配置
│
├── tracing/                      ← 【链路追踪模块】
├── util/                         ← 【工具类】
│
└── src/test/java/                ← 【测试代码】
    ├── controller/                   控制器测试
    ├── service/                      服务层测试
    ├── circuitbreaker/               熔断器测试
    ├── sentinel/                     Sentinel 测试
    ├── logging/                      日志脱敏测试
    ├── virtual/                      虚拟线程测试
    ├── mapper/                       映射器测试
    ├── integration/                  集成测试
    └── ...                           约 40+ 测试文件
```

---

## 五、API 接口速查

### 5.1 基础测试 `/test`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/test/hello` | 健康检查 |
| POST | `/test/echo` | 回显请求体 |
| POST | `/test/delay` | 模拟延迟 (`{"second": 3}`) |
| GET | `/test/user` | MapStruct 映射演示 |

### 5.2 异步处理 `/async`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/async/completable-future/{taskName}?delaySeconds=5` | CompletableFuture 异步 |
| POST | `/async/deferred-result` | DeferredResult 异步 |
| GET | `/async/callable/{delaySeconds}` | Callable 异步 |
| GET | `/async/concurrent-test?taskCount=3` | 并发任务测试 |
| GET | `/async/metrics` | 异步指标查询 |

### 5.3 重试机制 `/retry`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/retry/basic` | 基本重试 |
| GET | `/retry/local` | @LocalRetryable 本地服务重试 |
| GET | `/retry/remote` | @RemoteRetryable 远程服务重试 |
| GET | `/retry/conditional` | 条件重试（按异常类型） |
| GET | `/retry/imperative` | 编程式 RetryTemplate |
| GET | `/retry/spel` | SpEL 表达式条件重试 |
| GET | `/retry/all-examples` | 一键运行所有重试示例 |

### 5.4 JOOQ 电商 `/api/jooq`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/jooq/users` | 创建用户 |
| GET | `/api/jooq/users` | 查询所有用户 |
| GET | `/api/jooq/users/{id}` | 按 ID 查用户 |
| POST | `/api/jooq/products` | 创建商品 |
| POST | `/api/jooq/products/batch` | 批量创建商品 |
| GET | `/api/jooq/products/low-stock` | 低库存商品 |
| POST | `/api/jooq/orders` | 创建订单（事务） |
| GET | `/api/jooq/orders/{id}` | 订单详情（多表 JOIN） |
| GET | `/api/jooq/orders/statistics` | 订单统计（聚合） |

### 5.5 虚拟线程 `/api/virtual`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/virtual/basic-task` | 基础虚拟线程任务 |
| GET | `/api/virtual/batch-tasks?taskCount=100` | 批量虚拟线程 |
| GET | `/api/virtual/pin-detection` | Pin 检测 |
| POST | `/api/virtual/pin-test?pinType=SYNCHRONIZED` | 测试 Pin 场景 |
| GET | `/api/virtual/scoped-value` | ScopedValue 演示 |
| POST | `/api/virtual/structured-concurrency?strategy=JOIN_ALL` | 结构化并发 |
| GET | `/api/virtual/performance-comparison` | 虚拟线程 vs 平台线程性能对比 |
| GET | `/api/virtual/demo-all` | 综合演示 |

### 5.6 熔断器 `/api/circuitbreaker`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/circuitbreaker/circuit-breaker` | 熔断器模式 |
| POST | `/api/circuitbreaker/rate-limiter` | 限流器模式 |
| POST | `/api/circuitbreaker/bulkhead` | 舱壁隔离模式 |
| POST | `/api/circuitbreaker/time-limiter` | 超时控制模式 |
| POST | `/api/circuitbreaker/all-resilience` | 组合所有容错模式 |
| GET | `/api/circuitbreaker/rate-limit/basic` | X-Caller 基础限流 |
| GET | `/api/circuitbreaker/state/{name}` | 熔断器状态 |
| GET | `/api/circuitbreaker/metrics` | 指标查询 |
| GET | `/api/circuitbreaker/demo-all` | 综合演示 |

### 5.7 Sentinel `/api/sentinel`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/sentinel/flow-control` | 流量控制 |
| GET | `/api/sentinel/degrade` | 熔断降级 |
| GET | `/api/sentinel/hotspot` | 热点参数限流 |
| GET | `/api/sentinel/system/cpu` | 系统 CPU 保护 |
| GET | `/api/sentinel/statistics` | 实时统计 |
| POST | `/api/sentinel/rules/flow` | 动态添加流控规则 |
| GET | `/api/sentinel/demo-all` | 综合演示 |

### 5.8 日志脱敏 `/api/logging/test`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/logging/test/email?email=xxx` | 邮箱脱敏测试 |
| POST | `/api/logging/test/phone?phone=xxx` | 手机号脱敏测试 |
| POST | `/api/logging/test/all` | 综合脱敏测试 |
| GET | `/api/logging/test/status` | 脱敏规则状态 |

### 5.9 监控端点 `/actuator`

| 路径 | 说明 |
|------|------|
| `/actuator/health` | 应用健康状态 |
| `/actuator/metrics` | 指标列表 |
| `/actuator/prometheus` | Prometheus 格式指标 |
| `/actuator/circuitbreakers` | 熔断器状态 |

---

## 六、数据库

### 6.1 配置

项目使用 **H2 内存数据库**，启动时自动执行 `schema.sql` 建表，无需手动配置。

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

### 6.2 表结构

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│   j_users    │     │  j_products  │     │    j_orders      │
├──────────────┤     ├──────────────┤     ├──────────────────┤
│ id (PK)      │     │ id (PK)      │     │ id (PK)          │
│ username     │     │ name         │     │ order_number     │
│ email        │     │ description  │     │ user_id (FK) ────│──→ j_users
│ phone        │     │ price        │     │ total_amount     │
│ created_at   │     │ stock        │     │ status           │
│ updated_at   │     │ category     │     │ remarks          │
└──────────────┘     │ created_at   │     │ created_at       │
                     └──────────────┘     └──────────────────┘
                                               │
                                               │ 1:N
                                               ▼
                                         ┌──────────────────┐
                                         │  j_order_items   │
                                         ├──────────────────┤
                                         │ id (PK)          │
                                         │ order_id (FK) ───│──→ j_orders
                                         │ product_id (FK) ─│──→ j_products
                                         │ quantity         │
                                         │ price            │
                                         │ subtotal         │
                                         └──────────────────┘
```

> H2 Console: `http://localhost:8080/h2-console`（如需开启，需在 properties 中添加 `spring.h2.console.enabled=true`）

---

## 七、编码规范速记

### ✅ 必须遵守

| 规则 | 正确 ✅ | 错误 ❌ |
|------|---------|---------|
| 依赖注入 | `@Resource` 或构造器注入 | ~~`@Autowired`~~ |
| 日志语言 | 日志消息用**英文** | ~~日志消息用中文~~ |
| 注释语言 | 代码注释用**中文** | ~~代码注释用英文~~ |
| 日志框架 | `@Slf4j` + `log.error("msg", e)` | ~~不传异常对象~~ |
| DTO 后缀 | `*Dto` | ~~`*DTO` / `*Request`~~ |
| VO 后缀 | `*Vo` | ~~`*VO` / `*Response`~~ |

### ❌ 禁止操作

1. **不使用 `@Autowired`** — 用 `@Resource`
2. **不删除测试** — 测试失败时修复代码
3. **不手动编辑 MapStruct 生成代码** — 修改接口定义，让编译器重新生成
4. **不修改 `AGENTS.md` 和 `CLAUDE.md`**

---

## 八、测试指南

### 8.1 运行测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=AsyncServiceTest

# 按包运行
mvn test -Dtest="com.example.demo.controller.*"

# 跳过测试快速构建
mvn clean install -DskipTests
```

### 8.2 测试约定

| 约定 | 说明 |
|------|------|
| 测试命名 | `*Test.java` |
| 测试风格 | Given-When-Then 模式 |
| Mock 框架 | `@MockitoBean`（Spring Boot 3.5+） |
| 集成测试 | `@Transactional` 确保回滚 |
| 异步测试 | MockMvc 的 `asyncDispatch` |
| 容器测试 | `@Testcontainers` + `@DynamicPropertySource` |

---

## 九、架构设计要点

### 9.1 分层架构

```
客户端请求
    │
    ▼
┌──────────────┐     全局异常处理（@Order 控制优先级）
│  Controller  │ ←── JooqExceptionHandler (@Order=1)
│              │ ←── AsyncExceptionHandler (@Order=2)
├──────────────┤
│    Service   │ ←── 业务逻辑，调用 Repository / Mapper
├──────────────┤
│  Repository  │ ←── 数据访问（目前仅 JOOQ 模块使用）
├──────────────┤
│   Mapper     │ ←── DTO ↔ Entity 转换（MapStruct / Orika）
├──────────────┤
│   Database   │ ←── H2 内存数据库
└──────────────┘
```

### 9.2 包结构模式

项目采用**混合分层与功能包**结构：
- **分层为主**：`controller/`, `service/`, `dto/`, `mapper/`, `repository/`
- **功能子包**：`service/jooq/`, `dto/jooq/`, `controller/jooq/`
- **独立模块**：`virtual/`, `circuitbreaker/`, `sentinel/`, `logging/` — 各自包含完整的 MVC 分层

### 9.3 异常处理链

```
请求异常
    │
    ├─→ @Order(1) JooqExceptionHandler   → 处理 JOOQ 业务异常
    ├─→ @Order(2) AsyncExceptionHandler  → 处理异步任务异常
    └─→ @Order(默认) 其他 Handler        → 兜底处理
```

---

## 十、常见场景操作指南

### 10.1 添加一个新的 REST 接口

```
1. 在 controller/ 下创建 Controller，使用 @Resource 注入 Service
2. 在 service/ 下创建 Service，编写业务逻辑
3. 在 dto/ 下创建入参 DTO（*Dto 后缀）
4. 在 vo/ 下创建出参 VO（*Vo 后缀）
5. 需要数据库？→ repository/ + entity/
6. 需要对象映射？→ mapper/ + MapStruct 接口
7. 在 src/test/ 下编写测试（Given-When-Then）
```

### 10.2 修改 JOOQ 电商模块

```
1. 修改 schema.sql → 更新表结构
2. 修改 repository/jooq/ → 更新数据访问
3. 修改 service/jooq/ → 更新业务逻辑
4. 修改 controller/jooq/ → 更新 API 接口
5. 运行 mvn test 验证
```

### 10.3 添加一个新的 Resilience4j 熔断器

```
1. 在 application.properties 添加实例配置
2. 在 circuitbreaker/service/ 中创建 Service 方法
3. 使用 @CircuitBreaker / @RateLimiter / @Bulkhead 注解
4. 在 circuitbreaker/controller/ 中暴露 API
5. 编写测试验证
```

---

## 十一、推荐学习路径

```
Week 1: 异步处理 → Spring Retry → 全局异常处理
         ↓
Week 2: JOOQ 电商系统 → MapStruct 映射
         ↓
Week 3: 虚拟线程 → 结构化并发
         ↓
Week 4: Resilience4j → Sentinel → 日志脱敏
         ↓
Week 5: 指标监控 → 链路追踪 → 性能优化
```

---

## 十二、故障排查

| 问题 | 解决方案 |
|------|---------|
| `java: error: release version 25 not supported` | 安装 JDK 25，更新 JAVA_HOME |
| 编译错误 `找不到符号` | `mvn clean compile` 重新生成 MapStruct 代码 |
| H2 表不存在 | 检查 `schema.sql` 是否在 `resources/` 下 |
| 端口 8080 被占用 | `server.port=8080` 改为其他端口 |
| Redis 连接失败 | 正常，Redis 不影响启动。如需可启动本地 Redis |
| 测试超时 | 检查 surefire `<argLine>--enable-preview</argLine>` 配置 |

---

## 十三、关键配置速查

| 配置项 | 值 | 文件 |
|--------|-----|------|
| 服务端口 | `8080` | `application.properties` |
| 异步线程池 | core=5, max=20, queue=100 | `application.properties` |
| 异步超时 | 30s | `application.properties` |
| 数据库 | H2 内存库 `testdb` | `application.properties` |
| 日志框架 | Log4j2 + Disruptor 异步 | `pom.xml` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | 自动配置 |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` | 自动配置 |
| Actuator | health,info,metrics,prometheus | `application.properties` |

---

## 十四、技术栈版本一览

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.5.9 |
| Java | 25 |
| JOOQ | 3.19.15 (Spring Boot 管理) |
| MapStruct | 1.6.3 |
| Lombok | 1.18.42 |
| Resilience4j | 2.3.0 |
| Sentinel | 1.8.8 |
| SpringDoc OpenAPI | 2.8.0 |
| Testcontainers | 1.20.4 |
| JUnit | 5.11.4 |
| Log4j2 + Disruptor | 4.0.0 |
| Micrometer + Prometheus | Spring Boot 管理 |

---

> 📌 **提示**: 本文档基于代码库自动生成，如有疑问请参考 `AGENTS.md` 或直接阅读源码。每个模块的 Controller 都有完整的 Swagger 注解，推荐通过 Swagger UI 交互式学习。
