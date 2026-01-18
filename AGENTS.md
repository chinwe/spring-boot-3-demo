# Spring Boot 3 Demo 项目知识库

**生成时间**: 2026-01-18
**提交哈希**: N/A
**分支**: N/A

---

## 概述

Spring Boot 3.5.9 + Java 21 演示项目，展示现代 Java Web 应用架构。包含四大功能模块：异步处理、Spring Retry、JOOQ 电商系统、对象映射（MapStruct + Orika）。

---

## 结构

```
spring-boot-3-demo/
├── src/main/java/com/example/demo/
│   ├── controller/           # HTTP 请求处理
│   │   ├── jooq/          # JOOQ 电商 API
│   │   └── orika/         # 对象映射演示
│   ├── service/            # 业务逻辑
│   │   ├── jooq/          # JOOQ 电商服务
│   │   └── orika/         # 映射服务
│   ├── repository/jooq/    # JOOQ 数据访问
│   ├── dto/               # 数据传输对象
│   │   └── jooq/         # JOOQ 专用 DTO
│   ├── mapper/             # MapStruct 映射器
│   │   └── orika/        # Orika 映射器
│   ├── vo/                # 值对象（视图）
│   ├── entity/            # 领域实体
│   ├── exception/         # 全局异常处理
│   ├── configuration/     # 配置类
│   ├── annotation/        # 自定义注解
│   └── listener/         # 事件监听器
├── src/test/java/         # 测试代码（17 个文件）
└── AGENTS.md              # 本文件
```

---

## 去哪里找

| 任务 | 位置 | 说明 |
|------|------|------|
| 异步任务配置 | `configuration/AsyncConfiguration` | 线程池配置 |
| 重试机制 | `service/RetryService` | 声明式/编程式重试 |
| JOOQ 电商 | `controller/jooq/JooqController` | 完整电商 API |
| MapStruct 映射 | `mapper/OrderMapper` | 高级映射示例 |
| 异常处理 | `exception/*ExceptionHandler` | 全局异常处理器 |
| 自定义注解 | `annotation/` | @LocalRetryable, @RemoteRetryable |

---

## 约定（偏离标准）

### 依赖注入

**必须使用 `@Resource` 而非 `@Autowired`**：
```java
@Resource
private AsyncService asyncService;

// 或使用构造器注入（JOOQ 服务层）
@RequiredArgsConstructor
public class JooqOrderService {
    private final JooqOrderRepository orderRepository;
}
```

### 命名约定

| 类型 | 后缀 | 示例 |
|------|------|------|
| DTO | `*Dto` | `AsyncTaskDto`, `JooqUserDto` |
| VO | `*Vo` | `AsyncTaskVo`, `DelayVo` |
| Mapper | `*Mapper` | `OrderMapper`, `UserMapper` |
| Entity | 简单名称 | `Order`, `User`, `Customer` |
| Repository | `*Repository` | `JooqOrderRepository` |

### 日志规范

- **日志消息使用英文**
- **注释使用中文**
- 使用 Lombok `@Slf4j`
- 错误日志必须包含异常对象：`log.error("error message", e)`

### 测试规范

- 使用 Given-When-Then 模式
- Mock 使用 `@MockitoBean`（Spring Boot 3.5+）
- 集成测试使用 `@Transactional` 确保回滚
- 异步测试使用 MockMvc 的 `asyncDispatch`

---

## 禁止模式（本项目）

1. ❌ **不使用 `@Autowired`** - 必须使用 `@Resource`
2. ❌ **不删除测试** - 测试失败时修复代码，而非删除测试
3. ❌ **不手动编辑 MapStruct 生成代码** - 生成的映射器不应修改
4. ❌ **不修改 CLAUDE.md** - 命令示例不应修改
5. ❌ **不使用类型断言** - 避免使用 `@ts-ignore` 等 TypeScript 相关注解

---

## 独特样式

### 异常处理器优先级

使用 `@Order` 控制异常处理器执行顺序：
```java
@Order(1)
@RestControllerAdvice
public class JooqExceptionHandler { /* 业务异常 */ }

@Order(2)
@RestControllerAdvice
public class AsyncExceptionHandler { /* 异步异常 */ }
```

### MapStruct + Lombok 集成

pom.xml 注解处理器顺序关键：
1. Lombok（必须在前）
2. MapStruct
3. lombok-mapstruct-binding

```xml
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

### 混合分层与功能包结构

项目采用混合模式：
- 分层为主：`controller/`, `service/`, `dto/`, `mapper/`
- 功能子包：`service/jooq/`, `dto/jooq/`, `controller/jooq/`

**注意**：`TestController.java` 在根包，应移至 `controller/`。

---

## 命令

```bash
# 构建与编译
mvn clean compile           # 清理并编译
mvn clean package           # 打包
mvn spring-boot:run        # 运行应用

# 测试
mvn test                   # 运行所有测试
mvn test -Dtest=AsyncServiceTest              # 单个测试类
mvn test -Dtest=com.example.demo.controller.* # 按包测试

# 快速构建
mvn clean install -DskipTests
```

---

## 注意事项

### 配置文件

- **唯一配置**：`application.properties`（无 YAML，无 profile-specific 配置）
- **H2 数据库**：内存数据库，schema.sql 自动执行
- **异步线程池**：core=5, max=20, queue=100

### 数据库表（JOOQ 电商）

- `j_users` - 用户表
- `j_products` - 商品表
- `j_orders` - 订单表
- `j_order_items` - 订单项表

### API 文档

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 测试框架

- JUnit 5 + Testcontainers
- 测试文件命名：`*Test.java`
- 集成测试使用 `@Testcontainers` + `@DynamicPropertySource`

### 待改进

1. **TestController 位置** - 应移至 `controller/` 包
2. **依赖注入混用** - 测试中少量 `@Autowired` 使用
3. **断言库混用** - 少量 AssertJ 使用，应统一为 JUnit 5 Assertions
4. **环境配置** - 仅 `application.properties`，缺少 profile-specific 配置

---

## 技术栈

- Spring Boot 3.5.9
- Java 21
- Maven
- JOOQ 3.19.15
- H2 Database
- Redis（已配置）
- Lombok 1.18.42
- MapStruct 1.6.3
- Orika 1.5.4
- Spring Retry
- SpringDoc OpenAPI 2.8.0
- Testcontainers 1.20.4
- JUnit 5.11.4
