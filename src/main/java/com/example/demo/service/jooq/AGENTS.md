# JOOQ Service Layer

## OVERVIEW
JOOQ 电商业务逻辑层，处理用户、商品、订单的 CRUD 和事务操作

## WHERE TO LOOK
| 任务 | 位置 | 说明 |
|------|------|------|
| 订单创建（事务） | `JooqTransactionService.createOrder()` | 完整订单事务流程 |
| 商品批量操作 | `JooqProductService.batchCreateProducts()` | 批量插入及验证 |
| 用户 CRUD | `JooqUserService` | 基础用户操作 |
| 订单统计 | `JooqOrderService.getOrderStatistics()` | 聚合查询 |

## CONVENTIONS

### 依赖注入
**使用 `@RequiredArgsConstructor` 构造器注入**（非项目通用 @Resource）：
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class JooqOrderService {
    private final JooqOrderRepository orderRepository;
}
```

### 事务管理
**仅 `JooqTransactionService` 使用事务**，使用 `@Transactional(rollbackFor = Exception.class)`：
```java
@Transactional(rollbackFor = Exception.class)
public Long createOrder(JooqCreateOrderRequest request) {
    // 库存验证、扣减、订单创建在同一事务
}
```

### 业务逻辑分离
- `JooqOrderService` - 订单查询（无事务）
- `JooqProductService` - 商品管理（无事务）
- `JooqUserService` - 用户管理（无事务）
- `JooqTransactionService` - 事务性操作（创建订单）

### 仓储层集成
服务层通过 Repository 访问 DSLContext，不直接注入：
```java
// ✅ 正确：通过 Repository
private final JooqOrderRepository orderRepository;

// ❌ 错误：直接注入 DSLContext
// private final DSLContext dslContext;
```

## ANTI-PATTERNS

1. ❌ **不在非事务服务中直接操作多张表** - 应通过 `JooqTransactionService`
2. ❌ **不在服务层注入 DSLContext** - 必须通过 Repository 层
3. ❌ **不在 OrderService/ProductService/UserService 中使用 `@Transactional`** - 仅 TransactionService 使用
4. ❌ **不在服务层捕获 `EntityNotFoundException`** - 交给全局异常处理器处理
