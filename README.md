# Spring Boot Demo 项目

## 项目概述

这是一个基于Spring Boot 3.5.0的演示项目，展示了现代Java Web应用程序的基本架构和功能。项目使用了多种流行的技术栈，包括Spring Boot、JOOQ、Redis、H2数据库等。

## 技术栈

### 核心框架
- **Spring Boot 3.5.0** - 主框架
- **Java 17** - 编程语言版本
- **Maven** - 项目构建工具

### 主要依赖
- **Spring Boot Web** - Web应用程序支持
- **Spring Boot Validation** - 数据验证
- **Spring Boot Data Redis** - Redis缓存支持
- **Spring Boot JOOQ** - 数据库访问层
- **H2 Database** - 内存数据库（开发/测试用）
- **Lombok** - 减少样板代码
- **MapStruct 1.6.2** - 对象映射工具
- **Testcontainers** - 集成测试支持

## 项目结构

```
src/main/java/com/example/demo/
├── DemoApplication.java          # 应用程序入口
├── TestController.java           # 测试控制器
├── configuration/                # 配置类目录（空）
├── controller/                   # 控制器目录（空）
├── dto/                         # 数据传输对象
│   └── UserDto.java
├── entity/                      # 实体类
│   └── User.java
├── exception/                   # 异常处理（空）
├── mapper/                      # 对象映射器
│   └── UserMapper.java
├── service/                     # 服务层
│   └── impl/                    # 服务实现（空）
└── vo/                          # 值对象
    └── DelayVo.java
```

## 核心功能

### 1. RESTful API 端点

项目提供了以下API端点：

#### GET /test/hello
- **功能**: 简单的问候接口
- **返回**: "Hello from TestController!"

#### POST /test/echo
- **功能**: 回显输入的消息
- **请求体**: 字符串消息
- **返回**: 原样返回输入的消息

#### POST /test/delay
- **功能**: 模拟延迟响应
- **请求体**: 
  ```json
  {
    "second": 5
  }
  ```
- **验证**: second字段必须在1-60之间
- **返回**: 延迟指定秒数后返回确认消息

#### POST /test/jooq
- **功能**: 演示JOOQ数据库查询
- **返回**: 当前时间戳

#### GET /test/user
- **功能**: 演示对象映射功能
- **返回**: 用户信息的字符串表示

### 2. 数据模型

#### User 实体
```java
public class User {
    private Long id;
    private String name;
    private String email;
}
```

#### UserDto 数据传输对象
```java
public class UserDto {
    private String name;
    private String email;
}
```

#### DelayVo 验证对象
```java
public class DelayVo {
    @NotNull
    @Min(value = 1)
    @Max(value = 60)
    private Integer second;
}
```

### 3. 对象映射

使用MapStruct进行实体和DTO之间的映射：
- `User` → `UserDto`: 包含所有字段
- `UserDto` → `User`: 忽略id字段

## 配置信息

### 应用配置 (application.properties)
```properties
# 服务器端口
server.port=8080

# H2内存数据库配置
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

## 如何运行

### 前置条件
- Java 17或更高版本
- Maven 3.6+

### 启动步骤
1. 克隆项目到本地
2. 在项目根目录执行：
   ```bash
   mvn clean install
   ```
3. 启动应用程序：
   ```bash
   mvn spring-boot:run
   ```
4. 应用程序将在 `http://localhost:8080` 启动

## API测试

项目包含了 `test.http` 文件，可以使用支持HTTP文件的IDE（如IntelliJ IDEA）直接测试API：

```http
# 问候接口
GET http://localhost:8080/test/hello

# 回显接口
POST http://localhost:8080/test/echo
Content-Type: application/json
{
    "name": "tom"
}

# 延迟接口
POST http://localhost:8080/test/delay
Content-Type: application/json
{
    "second": 5
}

# JOOQ查询接口
POST http://localhost:8080/test/jooq

# 用户信息接口
GET http://localhost:8080/test/user
```

## 开发特性

### 1. 代码简化
- 使用Lombok注解减少样板代码
- `@Data` 自动生成getter/setter
- `@AllArgsConstructor` 自动生成全参构造器

### 2. 数据验证
- 使用Bean Validation进行请求参数验证
- 支持自定义验证规则

### 3. 对象映射
- MapStruct提供高性能的对象映射
- 编译时生成映射代码，无反射开销

### 4. 数据库访问
- JOOQ提供类型安全的SQL查询
- H2内存数据库便于开发和测试

## 扩展建议

这个项目为进一步开发提供了良好的基础架构，可以考虑以下扩展：

1. **完善业务逻辑**: 在service层添加具体的业务逻辑
2. **数据持久化**: 配置生产环境数据库（MySQL、PostgreSQL等）
3. **安全认证**: 集成Spring Security
4. **API文档**: 集成Swagger/OpenAPI
5. **缓存策略**: 利用已配置的Redis进行缓存
6. **异常处理**: 完善全局异常处理机制
7. **单元测试**: 利用Testcontainers编写集成测试

## 作者

- chinw

## JOOQ 电商订单系统示例

项目包含一个完整的 JOOQ 电商订单系统示例，展示了 JOOQ 的各种功能。

### 功能特性

- **CRUD 操作**: 用户、商品、订单的基础增删改查
- **复杂查询**: JOIN、子查询、聚合函数、分组统计
- **事务管理**: 创建订单的完整事务流程
- **高级特性**: 批量操作、Upsert、分页查询

### API 文档

访问 Swagger UI 查看完整 API 文档:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### 主要接口

#### 用户管理
- `POST /api/jooq/users` - 创建用户
- `GET /api/jooq/users/{id}` - 查询用户
- `GET /api/jooq/users` - 查询所有用户
- `GET /api/jooq/users/username/{username}` - 根据用户名查询

#### 商品管理
- `POST /api/jooq/products` - 创建商品
- `POST /api/jooq/products/batch` - 批量创建商品
- `GET /api/jooq/products/{id}` - 查询商品
- `GET /api/jooq/products?category=Electronics&page=0&size=10` - 按分类分页查询
- `GET /api/jooq/products/low-stock?threshold=10` - 查询低库存商品
- `GET /api/jooq/products/stock/{category}` - 查询分类库存统计

#### 订单管理
- `POST /api/jooq/orders` - 创建订单（事务操作）
- `GET /api/jooq/orders/{id}` - 查询订单详情（包含用户和商品信息）
- `GET /api/jooq/users/{userId}/orders` - 查询用户订单列表
- `GET /api/jooq/orders/statistics` - 订单统计信息

### 使用示例

#### 创建订单示例

```bash
curl -X POST http://localhost:8080/api/jooq/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ],
    "remarks": "请尽快发货"
  }'
```

#### 查询低库存商品

```bash
curl http://localhost:8080/api/jooq/products/low-stock?threshold=10
```

#### 订单统计

```bash
curl http://localhost:8080/api/jooq/orders/statistics
```

---

这个项目展示了现代Spring Boot应用程序的标准架构模式，适合作为新项目的起始模板或学习参考。