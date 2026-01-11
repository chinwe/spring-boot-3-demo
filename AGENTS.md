# AGENTS.md

代码库中 agentic coding agents 的开发指南。

---

## 项目信息

- **Spring Boot**: 3.5.9
- **Java**: 21
- **构建工具**: Maven
- **测试框架**: JUnit 5 + Testcontainers

---

## 构建/Lint/测试命令

```bash
# 清理并编译项目
mvn clean compile

# 编译主代码
mvn compile

# 编译测试代码
mvn test-compile

# 打包项目
mvn clean package

# 运行应用程序
mvn spring-boot:run

# 跳过测试快速构建
mvn clean install -DskipTests

# 运行所有测试
mvn test

# 运行特定测试类（单文件）
mvn test -Dtest=AsyncServiceTest
mvn test -Dtest=RetryControllerTest
mvn test -Dtest=OrderMapperTest
mvn test -Dtest=JooqIntegrationTest

# 运行特定包下的所有测试
mvn test -Dtest=com.example.demo.controller.*
mvn test -Dtest=com.example.demo.service.jooq.*

# 运行集成测试
mvn test -Dtest=AsyncIntegrationTest
```

---

## 代码风格指南

### 导入顺序

1. Java 标准库
2. 第三方库（按字母顺序）
3. Spring 相关（jakarta.*, org.springframework.*）
4. 项目内部包（com.example.demo.*）

### 命名约定

| 类型 | 约定 | 示例 |
|------|------|------|
| 类 | PascalCase | `AsyncService.java` |
| 接口 | PascalCase | `UserRepository.java` |
| 方法 | camelCase | `performLongRunningTask()` |
| 常量 | UPPER_SNAKE_CASE | `DATE_FORMATTER` |
| 枚举 | PascalCase | `TaskStatus.PENDING` |
| 包 | 小写点分隔 | `com.example.demo.service.jooq` |
| DTO | *Dto | `AsyncTaskDto` |
| VO | *Vo | `AsyncTaskVo` |
| Entity | 简单名称 | `Order.java` |
| Mapper | *Mapper | `OrderMapper.java` |

### 依赖注入

**使用 `@Resource` 而不是 `@Autowired`**:
```java
@Resource
private AsyncService asyncService;
```

### 日志

1. 使用 Lombok `@Slf4j` 注解
2. **日志消息使用英文**
3. **注释使用中文**
4. 日志级别：
   - `log.error()`: 错误和异常（必须包含异常对象）
   - `log.warn()`: 警告信息
   - `log.info()`: 重要业务流程
   - `log.debug()`: 调试信息

```java
@Slf4j
@Service
public class ExampleService {
    public void doSomething(String input) {
        log.info("Starting operation with input: {}", input);
        try {
            // 业务逻辑
            log.info("Operation completed successfully");
        } catch (Exception e) {
            log.error("Operation failed for input: {}", input, e);
        }
    }
}
```

### 异常处理

1. 自定义异常类继承自 `RuntimeException` 或 `Exception`
2. 使用 `@RestControllerAdvice` + `@Order` 处理全局异常
3. 异常排除：检查实例类型后 `throw ex` 转发到其他处理器

```java
@Slf4j
@Order(2)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("Custom exception occurred", ex);
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // 转发特定异常到其他处理器
        if (ex instanceof AnotherException) {
            throw ex;
        }
        log.error("Runtime exception occurred", ex);
        return ResponseEntity.internalServerError().body(new ErrorResponse("Internal error"));
    }
}
```

### Lombok 注解使用

1. DTO/VO: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
2. Entity: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
3. Service/Controller: `@Slf4j` + Spring 注解

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskDto {
    private String taskId;
    private TaskStatus status;
    private LocalDateTime startTime;
}
```

### MapStruct 配置

1. Mapper 使用 `@Mapper(componentModel = "spring")`
2. pom.xml 中注解处理器顺序：**Lombok → MapStruct → lombok-mapstruct-binding**
3. 使用 `@Named`, `@BeforeMapping`, `@AfterMapping` 实现高级映射

```java
@Mapper(componentModel = "spring", uses = {OtherMapper.class})
public abstract class OrderMapper {

    @Mapping(target = "createdAt", ignore = true)
    public abstract OrderDto toDto(Order order);

    @Named("formatDate")
    protected String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
```

### 测试代码风格

1. 使用 **Given-When-Then** 模式
2. 使用 `@SpringBootTest` 或 `@WebMvcTest`
3. 断言使用 JUnit 5: `assertEquals`, `assertNotNull`, `assertTrue`
4. Mock 使用 `@MockitoBean` (Spring Boot 3.5+) 或 `@Mock`

```java
@SpringBootTest
class AsyncServiceTest {

    @Resource
    private AsyncService asyncService;

    @Test
    void testPerformLongRunningTaskSuccess() {
        // Given
        String taskName = "test-task";
        int delaySeconds = 1;

        // When
        CompletableFuture<AsyncTaskDto> future = asyncService.performLongRunningTask(taskName, delaySeconds, false);

        // Then
        AsyncTaskDto result = future.join();
        assertNotNull(result);
        assertEquals(AsyncTaskDto.TaskStatus.COMPLETED, result.getStatus());
    }
}
```

### 代码与注释规范

1. **所有代码、日志、消息使用英文**
2. **所有注释使用中文**
3. 类级别注释使用 Javadoc

```java
/**
 * 异步服务
 *
 * 负责处理异步任务执行、指标收集等操作
 *
 * @author chinwe
 */
@Slf4j
@Service
public class AsyncService {

    /**
     * 执行长时间运行的任务
     *
     * @param taskName 任务名称
     * @param delaySeconds 延迟秒数
     * @param shouldFail 是否失败
     * @return 异步任务结果
     */
    public CompletableFuture<AsyncTaskDto> performLongRunningTask(
            String taskName, int delaySeconds, boolean shouldFail) {
        // Implementation in English
    }
}
```

### 分层架构

严格遵守以下层次：

```
controller/     # HTTP 请求处理，参数验证
    ↓
service/        # 业务逻辑
    ↓
repository/     # 数据访问（JOOQ）
    ↓
dto/            # 数据传输对象（API 交互）
vo/             # 值对象（视图展示）
entity/         # 领域实体
mapper/         # 对象映射（MapStruct）
```

### 验证

使用 Jakarta Validation (`@Valid`, `@NotNull`, `@Min`, `@Max`, `@Pattern`):

```java
@PostMapping("/products")
public ProductDto createProduct(@Valid @RequestBody CreateProductRequest request) {
    // 自动验证请求参数
}

// DTO 中
public class CreateProductRequest {
    @NotBlank
    private String name;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;
}
```

---

## 重要注意事项

1. **不要使用** `@Autowired`，使用 `@Resource`
2. **不要使用** 类型断言 (`as any`, `@ts-ignore`)
3. **不要修改** CLAUDE.md 中的命令示例
4. MapStruct 生成代码不要手动编辑
5. 测试失败时不要删除测试，应修复代码
6. 异常处理器使用 `@Order` 控制执行顺序
