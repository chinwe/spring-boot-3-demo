# JOOQ DTO Package

## OVERVIEW
JOOQ 电商专用数据传输对象，包含用户、商品、订单及其关联对象的验证和映射

## WHERE TO LOOK
| 任务 | 位置 | 说明 |
|------|------|------|
| 用户 DTO | `JooqUserDto` | 用户信息及邮箱/手机验证 |
| 订单 DTO | `JooqOrderDto` | 订单主体及嵌套订单项列表 |
| 订单项 DTO | `JooqOrderItemDto` | 订单项明细 |
| 商品 DTO | `JooqProductDto` | 商品基本信息 |
| 创建订单请求 | `JooqCreateOrderRequest` | 嵌套 OrderItemRequest 静态类 |
| 创建商品请求 | `JooqCreateProductRequest` | 商品创建验证规则 |
| 订单查询请求 | `JooqOrderQueryRequest` | 订单查询条件 |

## CONVENTIONS

### 命前缀规范
所有类名前缀为 "Jooq"（`JooqUserDto`, `JooqOrderDto`, `JooqCreateOrderRequest` 等）

### Jakarta Validation
使用完整的 Jakarta Validation 注解组合验证业务规则：
```java
@NotBlank(message = "用户名不能为空")
@Email(message = "邮箱格式不正确")
@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
@NotNull(message = "价格不能为空")
@DecimalMin(value = "0.01", message = "价格必须大于0")
@Digits(integer = 10, fraction = 2, message = "价格最多2位小数")
@Size(min = 1, message = "至少包含一个订单项")
@Valid
```

### 嵌套 DTO 结构
`JooqOrderDto` 包含 `List<JooqOrderItemDto>`，关联查询时填充关联字段：
```java
@Data
@Builder
public class JooqOrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String username; // 关联查询时填充
    private List<JooqOrderItemDto> items; // 嵌套订单项
}
```

### 请求 DTO 区分
Request 类用于接收用户请求（带完整验证），Dto 类用于数据传输：
- `JooqCreateOrderRequest` - 创建订单请求
- `JooqCreateProductRequest` - 创建商品请求
- `JooqOrderQueryRequest` - 订单查询条件

### Lombok 组合模式
统一使用四注解组合：
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqUserDto {
    // 字段定义
}
```

## ANTI-PATTERNS

1. ❌ **不在静态嵌套类中省略 Lombok 注解** - `OrderItemRequest` 必须同样使用 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
2. ❌ **不在嵌套 DTO 中使用 `@Valid`** - 父 DTO 的列表字段必须标注 `@Valid` 触发级联验证
3. ❌ **不省略验证消息** - 所有 `@NotBlank`、`@NotNull` 等注解必须包含 `message` 参数
4. ❌ **不将关联字段作为持久化字段** - 如 `JooqOrderDto.username` 仅在 JOIN 查询时填充，不应作为持久化字段
