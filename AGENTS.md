# Spring Boot 3 Demo 项目知识库

> Spring Boot 3.5.9 + Java 25，展示现代 Java Web 应用架构。
> 五大模块：异步处理、Spring Retry、JOOQ 电商、MapStruct 对象映射、虚拟线程。

## 去哪里找

| 任务 | 位置 | 任务 | 位置 |
|------|------|------|------|
| 异步线程池 | `configuration/AsyncConfiguration` | 重试机制 | `service/RetryService` |
| JOOQ 电商 API | `controller/jooq/JooqController` | MapStruct 映射 | `mapper/OrderMapper` |
| 全局异常处理 | `exception/*ExceptionHandler` | 自定义注解 | `annotation/` |
| 虚拟线程 | `virtual/` | 电商数据访问 | `repository/jooq/` |

包结构：分层为主（`controller/` `service/` `dto/` `mapper/`），功能子包（`*/jooq/` `virtual/`）。
⚠ `TestController.java` 在根包，应移至 `controller/`。

## 约定

- **依赖注入**：必须用 `@Resource`，禁止 `@Autowired`；构造器注入用 `@RequiredArgsConstructor`
- **命名**：`*Dto` / `*Vo` / `*Mapper` / `*Repository`，Entity 用简单名称
- **日志**：英文日志、中文注释、`@Slf4j`，错误日志须含异常 `log.error("msg", e)`
- **测试**：Given-When-Then | `@MockitoBean` | 集成测试 `@Transactional` | 异步 `asyncDispatch` | `*Test.java`
- **异常处理器**：用 `@Order` 控制优先级：业务异常(1) → 异步异常(2)
- **MapStruct**：pom.xml 注解处理器顺序 **Lombok → MapStruct → lombok-mapstruct-binding**（不可调换）

## 禁止模式

❌ 不使用 `@Autowired` → 用 `@Resource`
❌ 不删除测试 → 修复代码而非删除
❌ 不手动编辑 MapStruct 生成代码
❌ 不修改 CLAUDE.md

## 命令

```bash
mvn clean compile                             # 编译
mvn clean package                             # 打包
mvn spring-boot:run                           # 运行
mvn test                                      # 全部测试
mvn test -Dtest=AsyncServiceTest              # 单个测试类
mvn test -Dtest=com.example.demo.controller.* # 按包测试
mvn clean install -DskipTests                 # 跳过测试构建
```

## 配置 & 数据库

- 配置：`application.properties`（无 YAML / profile）| H2 内存库，`schema.sql` 自动建表
- 线程池 core=5, max=20, queue=100 | 测试：JUnit 5 + Testcontainers + `@DynamicPropertySource`
- JOOQ 表：`j_users` | `j_products` | `j_orders` | `j_order_items`
- API 文档：`/swagger-ui.html` | `/v3/api-docs`

## 技术栈

Spring Boot 3.5.9 · Java 25 · Maven · JOOQ 3.19.15 · H2 · Redis · Lombok 1.18.42
MapStruct 1.6.3 · Spring Retry · SpringDoc OpenAPI 2.8.0 · Testcontainers 1.20.4 · JUnit 5.11.4 · 虚拟线程

