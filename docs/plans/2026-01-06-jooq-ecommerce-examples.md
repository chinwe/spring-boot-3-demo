# JOOQ 电商订单示例实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 增加一个完整的 JOOQ 电商订单系统示例模块，展示 CRUD、复杂查询、事务管理和高级特性。

**架构:** 创建独立的 JOOQ 模块，包含 Controller、Service、Repository 三层架构。使用 H2 内存数据库，通过 SQL 脚本初始化表结构。采用 TDD 开发模式，每个功能先编写测试再实现。

**技术栈:** Spring Boot 3.5.9, Java 21, JOOQ (spring-boot-starter-jooq), H2 Database, JUnit 5, Mockito

---

## 前置准备

### Task 1: 创建数据库表结构

**文件:**
- 创建: `src/main/resources/schema.sql`

**Step 1: 创建 schema.sql 文件**

```sql
-- JOOQ 电商示例表结构

-- 用户表
CREATE TABLE IF NOT EXISTS j_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS j_products (
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
CREATE TABLE IF NOT EXISTS j_orders (
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
CREATE TABLE IF NOT EXISTS j_order_items (
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

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON j_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON j_orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON j_orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON j_order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON j_order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON j_products(category);
```

**Step 2: 配置 application.properties 自动执行 SQL**

编辑: `src/main/resources/application.properties`

在文件末尾添加:

```properties
# JOOQ 配置 - 自动执行 SQL 脚本
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

**Step 3: 验证配置**

运行: `mvn clean compile`

预期: 编译成功，无错误

**Step 4: 提交**

```bash
git add src/main/resources/schema.sql src/main/resources/application.properties
git commit -m "feat(jooq): add database schema and configuration"
```

---

### Task 2: 创建 DTO 类

**文件:**
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqUserDto.java`
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqProductDto.java`
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqOrderDto.java`
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqOrderItemDto.java`
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqCreateOrderRequest.java`
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqOrderQueryRequest.java`
- 创建: `src/main/java/com/example/demo/dto/jooq/JooqCreateProductRequest.java`

**Step 1: 创建 JooqUserDto**

```java
package com.example.demo.dto.jooq;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 用户 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqUserDto {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

**Step 2: 创建 JooqProductDto**

```java
package com.example.demo.dto.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 商品 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqProductDto {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

**Step 3: 创建 JooqOrderItemDto**

```java
package com.example.demo.dto.jooq;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 订单项 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqOrderItemDto {

    private Long id;

    private Long orderId;

    private Long productId;

    private String productName; // 关联查询时填充

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}
```

**Step 4: 创建 JooqOrderDto**

```java
package com.example.demo.dto.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 订单 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqOrderDto {

    private Long id;

    private String orderNumber;

    private Long userId;

    private String username; // 关联查询时填充

    private BigDecimal totalAmount;

    private String status;

    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<JooqOrderItemDto> items;
}
```

**Step 5: 创建 JooqCreateProductRequest**

```java
package com.example.demo.dto.jooq;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建商品请求
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqCreateProductRequest {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    private String category;
}
```

**Step 6: 创建 JooqCreateOrderRequest**

```java
package com.example.demo.dto.jooq;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建订单请求
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqCreateOrderRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "订单项不能为空")
    @Size(min = 1, message = "至少包含一个订单项")
    @Valid
    private List<OrderItemRequest> items;

    private String remarks;

    /**
     * 订单项请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
```

**Step 7: 创建 JooqOrderQueryRequest**

```java
package com.example.demo.dto.jooq;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询请求
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqOrderQueryRequest {

    private Long userId;

    private String status;

    private String orderNumber;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
```

**Step 8: 验证编译**

运行: `mvn clean compile`

预期: 编译成功，无错误

**Step 9: 提交**

```bash
git add src/main/java/com/example/demo/dto/jooq/
git commit -m "feat(jooq): add DTO classes for e-commerce module"
```

---

### Task 3: 创建 Repository 层 - 用户仓库

**文件:**
- 创建: `src/main/java/com/example/demo/repository/jooq/JooqUserRepository.java`

**Step 1: 编写测试**

创建: `src/test/java/com/example/demo/repository/jooq/JooqUserRepositoryTest.java`

```java
package com.example.demo.repository.jooq;

import static org.junit.jupiter.api.Assertions.*;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqUserDto;

/**
 * JooqUserRepository 测试类
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqUserRepositoryTest {

    @Autowired
    private DSLContext dsl;

    private JooqUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JooqUserRepository(dsl);
    }

    @Test
    void testInsertUser() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .phone("13800138000")
            .build();

        // When
        Long id = repository.insert(user);

        // Then
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    void testFindById() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        Long id = repository.insert(user);

        // When
        JooqUserDto found = repository.findById(id);

        // Then
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testFindByIdNotFound() {
        // When
        JooqUserDto found = repository.findById(99999L);

        // Then
        assertNull(found);
    }

    @Test
    void testFindByUsername() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        repository.insert(user);

        // When
        JooqUserDto found = repository.findByUsername("testuser");

        // Then
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testUpdate() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .phone("13800138000")
            .build();
        Long id = repository.insert(user);
        user.setId(id);
        user.setPhone("13900139000");

        // When
        boolean updated = repository.update(user);

        // Then
        assertTrue(updated);
        JooqUserDto updatedUser = repository.findById(id);
        assertEquals("13900139000", updatedUser.getPhone());
    }

    @Test
    void testDelete() {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        Long id = repository.insert(user);

        // When
        boolean deleted = repository.delete(id);

        // Then
        assertTrue(deleted);
        assertNull(repository.findById(id));
    }

    @Test
    void testFindAll() {
        // Given
        repository.insert(JooqUserDto.builder().username("user1").email("user1@example.com").build());
        repository.insert(JooqUserDto.builder().username("user2").email("user2@example.com").build());

        // When
        var users = repository.findAll();

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }
}
```

**Step 2: 运行测试验证失败**

运行: `mvn test -Dtest=JooqUserRepositoryTest`

预期: FAIL - "class JooqUserRepository not found"

**Step 3: 实现最小代码使测试通过**

创建: `src/main/java/com/example/demo/repository/jooq/JooqUserRepository.java`

```java
package com.example.demo.repository.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.jooq.JooqUserDto;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

/**
 * JOOQ 用户仓库
 * 展示基础 CRUD 操作
 *
 * @author chinwe
 */
@Repository
public class JooqUserRepository {

    private final DSLContext dsl;

    // 表定义
    private static final String TABLE_NAME = "j_users";

    public JooqUserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 插入用户
     *
     * @param user 用户 DTO
     * @return 生成的 ID
     */
    public Long insert(JooqUserDto user) {
        Record record = dsl.insertInto(
                table(TABLE_NAME),
                field("username"), field("email"), field("phone"),
                field("created_at"), field("updated_at")
            )
            .values(
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                LocalDateTime.now(),
                LocalDateTime.now()
            )
            .returning(field("id"))
            .fetchOne();

        return record.get(field("id", Long.class));
    }

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户 DTO，不存在返回 null
     */
    public JooqUserDto findById(Long id) {
        Record record = dsl.selectFrom(table(TABLE_NAME))
            .where(field("id").eq(id))
            .fetchOne();

        return record != null ? mapToUserDto(record) : null;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户 DTO，不存在返回 null
     */
    public JooqUserDto findByUsername(String username) {
        Record record = dsl.selectFrom(table(TABLE_NAME))
            .where(field("username").eq(username))
            .fetchOne();

        return record != null ? mapToUserDto(record) : null;
    }

    /**
     * 更新用户
     *
     * @param user 用户 DTO
     * @return 是否更新成功
     */
    public boolean update(JooqUserDto user) {
        int affected = dsl.update(table(TABLE_NAME))
            .set(field("username"), user.getUsername())
            .set(field("email"), user.getEmail())
            .set(field("phone"), user.getPhone())
            .set(field("updated_at"), LocalDateTime.now())
            .where(field("id").eq(user.getId()))
            .execute();

        return affected > 0;
    }

    /**
     * 删除用户
     *
     * @param id 用户 ID
     * @return 是否删除成功
     */
    public boolean delete(Long id) {
        int affected = dsl.deleteFrom(table(TABLE_NAME))
            .where(field("id").eq(id))
            .execute();

        return affected > 0;
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    public List<JooqUserDto> findAll() {
        Result<Record> records = dsl.selectFrom(table(TABLE_NAME))
            .fetch();

        return records.map(this::mapToUserDto);
    }

    /**
     * 将 Record 映射为 UserDto
     */
    private JooqUserDto mapToUserDto(Record record) {
        return JooqUserDto.builder()
            .id(record.get(field("id", Long.class)))
            .username(record.get(field("username", String.class)))
            .email(record.get(field("email", String.class)))
            .phone(record.get(field("phone", String.class)))
            .createdAt(record.get(field("created_at", LocalDateTime.class)))
            .updatedAt(record.get(field("updated_at", LocalDateTime.class)))
            .build();
    }
}
```

**Step 4: 运行测试验证通过**

运行: `mvn test -Dtest=JooqUserRepositoryTest`

预期: PASS - 所有测试通过

**Step 5: 提交**

```bash
git add src/test/java/com/example/demo/repository/jooq/JooqUserRepositoryTest.java
git add src/main/java/com/example/demo/repository/jooq/JooqUserRepository.java
git commit -m "feat(jooq): implement UserRepository with CRUD operations"
```

---

### Task 4: 创建 Repository 层 - 商品仓库（展示高级特性）

**文件:**
- 创建: `src/main/java/com/example/demo/repository/jooq/JooqProductRepository.java`
- 创建: `src/test/java/com/example/demo/repository/jooq/JooqProductRepositoryTest.java`

**Step 1: 编写测试**

创建: `src/test/java/com/example/demo/repository/jooq/JooqProductRepositoryTest.java`

```java
package com.example.demo.repository.jooq;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqProductDto;

/**
 * JooqProductRepository 测试类
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqProductRepositoryTest {

    @Autowired
    private DSLContext dsl;

    private JooqProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JooqProductRepository(dsl);
    }

    @Test
    void testBatchInsert() {
        // Given
        List<JooqProductDto> products = List.of(
            createProduct("Product 1", new BigDecimal("100.00"), 10, "Electronics"),
            createProduct("Product 2", new BigDecimal("200.00"), 20, "Electronics"),
            createProduct("Product 3", new BigDecimal("50.00"), 30, "Books")
        );

        // When
        int[] affected = repository.batchInsert(products);

        // Then
        assertEquals(3, affected.length);
        assertEquals(1, affected[0]);
        assertEquals(1, affected[1]);
        assertEquals(1, affected[2]);
    }

    @Test
    void testUpsert() {
        // Given - 先插入
        JooqProductDto product = createProduct("Product 1", new BigDecimal("100.00"), 10, "Electronics");
        Long id = repository.insert(product);
        product.setId(id);

        // When - 更新
        product.setPrice(new BigDecimal("150.00"));
        product.setStock(20);
        boolean upserted = repository.upsert(product);

        // Then
        assertTrue(upserted);
        JooqProductDto updated = repository.findById(id);
        assertEquals(new BigDecimal("150.00"), updated.getPrice());
        assertEquals(20, updated.getStock());
    }

    @Test
    void testUpsertNew() {
        // Given - 新商品
        JooqProductDto product = createProduct("New Product", new BigDecimal("100.00"), 10, "Electronics");
        product.setId(99999L);

        // When
        boolean upserted = repository.upsert(product);

        // Then
        assertTrue(upserted);
    }

    @Test
    void testFindByCategoryWithPagination() {
        // Given
        repository.batchInsert(List.of(
            createProduct("Product 1", new BigDecimal("100.00"), 10, "Electronics"),
            createProduct("Product 2", new BigDecimal("200.00"), 20, "Electronics"),
            createProduct("Product 3", new BigDecimal("300.00"), 30, "Electronics"),
            createProduct("Product 4", new BigDecimal("50.00"), 15, "Books")
        ));

        // When - 第一页
        List<JooqProductDto> page1 = repository.findByCategory("Electronics", 0, 2);

        // Then
        assertEquals(2, page1.size());

        // When - 第二页
        List<JooqProductDto> page2 = repository.findByCategory("Electronics", 2, 2);

        // Then
        assertEquals(1, page2.size());
    }

    @Test
    void testDecreaseStock() {
        // Given
        JooqProductDto product = createProduct("Product 1", new BigDecimal("100.00"), 10, "Electronics");
        Long id = repository.insert(product);

        // When
        boolean updated = repository.decreaseStock(id, 3);

        // Then
        assertTrue(updated);
        JooqProductDto updatedProduct = repository.findById(id);
        assertEquals(7, updatedProduct.getStock());
    }

    @Test
    void testDecreaseStockInsufficient() {
        // Given
        JooqProductDto product = createProduct("Product 1", new BigDecimal("100.00"), 5, "Electronics");
        Long id = repository.insert(product);

        // When
        boolean updated = repository.decreaseStock(id, 10);

        // Then
        assertFalse(updated);
    }

    @Test
    void testGetTotalStockByCategory() {
        // Given
        repository.batchInsert(List.of(
            createProduct("Product 1", new BigDecimal("100.00"), 10, "Electronics"),
            createProduct("Product 2", new BigDecimal("200.00"), 20, "Electronics"),
            createProduct("Product 3", new BigDecimal("50.00"), 30, "Books")
        ));

        // When
        int electronicsStock = repository.getTotalStockByCategory("Electronics");
        int booksStock = repository.getTotalStockByCategory("Books");

        // Then
        assertEquals(30, electronicsStock);
        assertEquals(30, booksStock);
    }

    @Test
    void testFindLowStockProducts() {
        // Given
        repository.batchInsert(List.of(
            createProduct("Product 1", new BigDecimal("100.00"), 3, "Electronics"),
            createProduct("Product 2", new BigDecimal("200.00"), 5, "Electronics"),
            createProduct("Product 3", new BigDecimal("300.00"), 20, "Electronics")
        ));

        // When
        List<JooqProductDto> lowStock = repository.findLowStockProducts(10);

        // Then
        assertEquals(2, lowStock.size());
    }

    private JooqProductDto createProduct(String name, BigDecimal price, int stock, String category) {
        return JooqProductDto.builder()
            .name(name)
            .price(price)
            .stock(stock)
            .category(category)
            .build();
    }
}
```

**Step 2: 运行测试验证失败**

运行: `mvn test -Dtest=JooqProductRepositoryTest`

预期: FAIL - "class JooqProductRepository not found"

**Step 3: 实现代码**

创建: `src/main/java/com/example/demo/repository/jooq/JooqProductRepository.java`

```java
package com.example.demo.repository.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.jooq.JooqProductDto;

import static org.jooq.impl.DSL.*;

/**
 * JOOQ 商品仓库
 * 展示高级特性：批量操作、Upsert、聚合查询、分页
 *
 * @author chinwe
 */
@Repository
public class JooqProductRepository {

    private final DSLContext dsl;

    private static final String TABLE_NAME = "j_products";

    public JooqProductRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 基础插入操作
     */
    public Long insert(JooqProductDto product) {
        Record record = dsl.insertInto(
                table(TABLE_NAME),
                field("name"), field("description"), field("price"),
                field("stock"), field("category"),
                field("created_at"), field("updated_at")
            )
            .values(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                LocalDateTime.now(),
                LocalDateTime.now()
            )
            .returning(field("id"))
            .fetchOne();

        return record.get(field("id", Long.class));
    }

    /**
     * 批量插入 - 展示批量操作
     */
    public int[] batchInsert(List<JooqProductDto> products) {
        return dsl.batch(
            products.stream()
                .map(p -> dsl.insertInto(
                        table(TABLE_NAME),
                        field("name"), field("description"), field("price"),
                        field("stock"), field("category"),
                        field("created_at"), field("updated_at")
                    )
                    .values(
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        p.getCategory(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                    )
                )
                .toList()
        ).execute();
    }

    /**
     * Upsert 操作 - 展示 MERGE 语法
     * 如果存在则更新，不存在则插入
     */
    public boolean upsert(JooqProductDto product) {
        // 使用 H2 的 MERGE 语法实现 UPSERT
        int affected = dsl.query(
            """
            MERGE INTO {0} (id, name, description, price, stock, category, updated_at)
            VALUES ({1}, {2}, {3}, {4}, {5}, {6}, {7})
            """,
            table(TABLE_NAME),
            val(product.getId()),
            val(product.getName()),
            val(product.getDescription()),
            val(product.getPrice()),
            val(product.getStock()),
            val(product.getCategory()),
            val(LocalDateTime.now())
        ).execute();

        return affected > 0;
    }

    /**
     * 分页查询 - 展示 LIMIT 和 OFFSET
     */
    public List<JooqProductDto> findByCategory(String category, int offset, int limit) {
        Result<Record> records = dsl.selectFrom(table(TABLE_NAME))
            .where(field("category").eq(category))
            .orderBy(field("id").asc())
            .limit(limit)
            .offset(offset)
            .fetch();

        return records.map(this::mapToProductDto);
    }

    /**
     * 原子性扣减库存 - 展示条件更新
     */
    public boolean decreaseStock(Long id, int quantity) {
        int affected = dsl.update(table(TABLE_NAME))
            .set(field("stock"), field("stock").sub(quantity))
            .set(field("updated_at"), LocalDateTime.now())
            .where(field("id").eq(id))
            .and(field("stock").ge(quantity)) // 库存充足才更新
            .execute();

        return affected > 0;
    }

    /**
     * 聚合查询 - 按分类统计库存
     */
    public int getTotalStockByCategory(String category) {
        Record1<Integer> record = dsl.select(sum(field("stock", Integer.class)))
            .from(table(TABLE_NAME))
            .where(field("category").eq(category))
            .fetchOne();

        return record.component1() != null ? record.component1() : 0;
    }

    /**
     * 复杂条件查询 - 查询低库存商品
     */
    public List<JooqProductDto> findLowStockProducts(int threshold) {
        Result<Record> records = dsl.selectFrom(table(TABLE_NAME))
            .where(field("stock").lt(threshold))
            .orderBy(field("stock").asc())
            .fetch();

        return records.map(this::mapToProductDto);
    }

    /**
     * 基础查询
     */
    public JooqProductDto findById(Long id) {
        Record record = dsl.selectFrom(table(TABLE_NAME))
            .where(field("id").eq(id))
            .fetchOne();

        return record != null ? mapToProductDto(record) : null;
    }

    private JooqProductDto mapToProductDto(Record record) {
        return JooqProductDto.builder()
            .id(record.get(field("id", Long.class)))
            .name(record.get(field("name", String.class)))
            .description(record.get(field("description", String.class)))
            .price(record.get(field("price", BigDecimal.class)))
            .stock(record.get(field("stock", Integer.class)))
            .category(record.get(field("category", String.class)))
            .createdAt(record.get(field("created_at", LocalDateTime.class)))
            .updatedAt(record.get(field("updated_at", LocalDateTime.class)))
            .build();
    }
}
```

**Step 4: 运行测试验证通过**

运行: `mvn test -Dtest=JooqProductRepositoryTest`

预期: PASS - 所有测试通过

**Step 5: 提交**

```bash
git add src/test/java/com/example/demo/repository/jooq/JooqProductRepositoryTest.java
git add src/main/java/com/example/demo/repository/jooq/JooqProductRepository.java
git commit -m "feat(jooq): implement ProductRepository with advanced features"
```

---

### Task 5: 创建 Repository 层 - 订单仓库（展示复杂查询）

**文件:**
- 创建: `src/main/java/com/example/demo/repository/jooq/JooqOrderRepository.java`
- 创建: `src/test/java/com/example/demo/repository/jooq/JooqOrderRepositoryTest.java`

**Step 1: 编写测试**

创建: `src/test/java/com/example/demo/repository/jooq/JooqOrderRepositoryTest.java`

```java
package com.example.demo.repository.jooq;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;

/**
 * JooqOrderRepository 测试类
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqOrderRepositoryTest {

    @Autowired
    private DSLContext dsl;

    private JooqOrderRepository orderRepository;
    private JooqUserRepository userRepository;
    private JooqProductRepository productRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new JooqOrderRepository(dsl);
        userRepository = new JooqUserRepository(dsl);
        productRepository = new JooqProductRepository(dsl);
    }

    @Test
    void testInsertOrderWithItems() {
        // Given
        Long userId = userRepository.insert(
            com.example.demo.dto.jooq.JooqUserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .build()
        );

        Long productId1 = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build()
        );

        Long productId2 = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 2")
                .price(new BigDecimal("200.00"))
                .stock(20)
                .build()
        );

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("300.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId1)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build(),
                JooqOrderItemDto.builder()
                    .productId(productId2)
                    .quantity(1)
                    .price(new BigDecimal("200.00"))
                    .subtotal(new BigDecimal("200.00"))
                    .build()
            ))
            .build();

        // When
        Long orderId = orderRepository.insertWithItems(order);

        // Then
        assertNotNull(orderId);
        assertTrue(orderId > 0);
    }

    @Test
    void testFindOrderWithItemsById() {
        // Given
        Long userId = userRepository.insert(
            com.example.demo.dto.jooq.JooqUserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build()
        );

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build()
            ))
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        // When
        JooqOrderDto found = orderRepository.findOrderWithItemsById(orderId);

        // Then
        assertNotNull(found);
        assertEquals("ORD001", found.getOrderNumber());
        assertNotNull(found.getItems());
        assertEquals(1, found.getItems().size());
        assertEquals(productId, found.getItems().get(0).getProductId());
    }

    @Test
    void testFindOrderWithUserById() {
        // Given
        Long userId = userRepository.insert(
            com.example.demo.dto.jooq.JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build()
        );

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build()
            ))
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        // When
        JooqOrderDto found = orderRepository.findOrderWithUserById(orderId);

        // Then
        assertNotNull(found);
        assertEquals("ORD001", found.getOrderNumber());
        assertEquals("john", found.getUsername());
    }

    @Test
    void testFindOrderWithUserAndItemsById() {
        // Given
        Long userId = userRepository.insert(
            com.example.demo.dto.jooq.JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build()
        );

        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build()
            ))
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        // When
        JooqOrderDto found = orderRepository.findOrderWithUserAndItemsById(orderId);

        // Then
        assertNotNull(found);
        assertEquals("ORD001", found.getOrderNumber());
        assertEquals("john", found.getUsername());
        assertNotNull(found.getItems());
        assertEquals(1, found.getItems().size());
    }

    @Test
    void testFindOrdersByUserId() {
        // Given
        Long userId = userRepository.insert(
            com.example.demo.dto.jooq.JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build()
        );

        JooqOrderDto order1 = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build()
            ))
            .build();

        JooqOrderDto order2 = JooqOrderDto.builder()
            .orderNumber("ORD002")
            .userId(userId)
            .totalAmount(new BigDecimal("200.00"))
            .status("SHIPPED")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(2)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("200.00"))
                    .build()
            ))
            .build();

        orderRepository.insertWithItems(order1);
        orderRepository.insertWithItems(order2);

        // When
        List<JooqOrderDto> orders = orderRepository.findOrdersByUserId(userId);

        // Then
        assertEquals(2, orders.size());
    }

    @Test
    void testGetOrderStatistics() {
        // Given
        Long userId = userRepository.insert(
            com.example.demo.dto.jooq.JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        Long productId = productRepository.insert(
            com.example.demo.dto.jooq.JooqProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build()
        );

        JooqOrderDto order1 = JooqOrderDto.builder()
            .orderNumber("ORD001")
            .userId(userId)
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(1)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("100.00"))
                    .build()
            ))
            .build();

        JooqOrderDto order2 = JooqOrderDto.builder()
            .orderNumber("ORD002")
            .userId(userId)
            .totalAmount(new BigDecimal("200.00"))
            .status("SHIPPED")
            .items(List.of(
                JooqOrderItemDto.builder()
                    .productId(productId)
                    .quantity(2)
                    .price(new BigDecimal("100.00"))
                    .subtotal(new BigDecimal("200.00"))
                    .build()
            ))
            .build();

        orderRepository.insertWithItems(order1);
        orderRepository.insertWithItems(order2);

        // When
        var stats = orderRepository.getOrderStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("total_orders"));
        assertTrue(stats.containsKey("total_amount"));
        assertTrue(stats.containsKey("pending_orders"));
        assertTrue(stats.containsKey("shipped_orders"));
    }
}
```

**Step 2: 运行测试验证失败**

运行: `mvn test -Dtest=JooqOrderRepositoryTest`

预期: FAIL - "class JooqOrderRepository not found"

**Step 3: 实现代码**

创建: `src/main/java/com/example/demo/repository/jooq/JooqOrderRepository.java`

```java
package com.example.demo.repository.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;

import static org.jooq.impl.DSL.*;

/**
 * JOOQ 订单仓库
 * 展示复杂查询：JOIN、聚合、分组统计
 *
 * @author chinwe
 */
@Repository
public class JooqOrderRepository {

    private final DSLContext dsl;

    private static final String ORDERS_TABLE = "j_orders";
    private static final String ORDER_ITEMS_TABLE = "j_order_items";
    private static final String USERS_TABLE = "j_users";
    private static final String PRODUCTS_TABLE = "j_products";

    public JooqOrderRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 插入订单及其订单项 - 展示多表操作
     */
    public Long insertWithItems(JooqOrderDto order) {
        // 1. 插入订单主表
        Record orderRecord = dsl.insertInto(
                table(ORDERS_TABLE),
                field("order_number"), field("user_id"), field("total_amount"),
                field("status"), field("remarks"),
                field("created_at"), field("updated_at")
            )
            .values(
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getRemarks(),
                LocalDateTime.now(),
                LocalDateTime.now()
            )
            .returning(field("id"))
            .fetchOne();

        Long orderId = orderRecord.get(field("id", Long.class));

        // 2. 批量插入订单项
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            var insertStatements = order.getItems().stream()
                .map(item -> dsl.insertInto(
                        table(ORDER_ITEMS_TABLE),
                        field("order_id"), field("product_id"),
                        field("quantity"), field("price"), field("subtotal")
                    )
                    .values(
                        orderId,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                    )
                )
                .toList();

            dsl.batch(insertStatements).execute();
        }

        return orderId;
    }

    /**
     * 查询订单及其订单项 - 展示一对多关联查询
     */
    public JooqOrderDto findOrderWithItemsById(Long orderId) {
        // 查询订单主表
        Record orderRecord = dsl.selectFrom(table(ORDERS_TABLE))
            .where(field("id").eq(orderId))
            .fetchOne();

        if (orderRecord == null) {
            return null;
        }

        JooqOrderDto order = mapToOrderDto(orderRecord);

        // 查询订单项
        Result<Record> itemRecords = dsl.selectFrom(table(ORDER_ITEMS_TABLE))
            .where(field("order_id").eq(orderId))
            .fetch();

        List<JooqOrderItemDto> items = itemRecords.map(this::mapToOrderItemDto);
        order.setItems(items);

        return order;
    }

    /**
     * 查询订单及用户信息 - 展示 INNER JOIN
     */
    public JooqOrderDto findOrderWithUserById(Long orderId) {
        // 使用 INNER JOIN 关联用户表
        Record record = dsl.select(
                field(ORDERS_TABLE + ".id"),
                field(ORDERS_TABLE + ".order_number"),
                field(ORDERS_TABLE + ".user_id"),
                field(ORDERS_TABLE + ".total_amount"),
                field(ORDERS_TABLE + ".status"),
                field(ORDERS_TABLE + ".remarks"),
                field(ORDERS_TABLE + ".created_at"),
                field(ORDERS_TABLE + ".updated_at"),
                field(USERS_TABLE + ".username").as("username")
            )
            .from(table(ORDERS_TABLE))
            .join(table(USERS_TABLE))
            .on(field(ORDERS_TABLE + ".user_id").eq(field(USERS_TABLE + ".id")))
            .where(field(ORDERS_TABLE + ".id").eq(orderId))
            .fetchOne();

        if (record == null) {
            return null;
        }

        return JooqOrderDto.builder()
            .id(record.get(field("id", Long.class)))
            .orderNumber(record.get(field("order_number", String.class)))
            .userId(record.get(field("user_id", Long.class)))
            .username(record.get(field("username", String.class)))
            .totalAmount(record.get(field("total_amount", BigDecimal.class)))
            .status(record.get(field("status", String.class)))
            .remarks(record.get(field("remarks", String.class)))
            .createdAt(record.get(field("created_at", LocalDateTime.class)))
            .updatedAt(record.get(field("updated_at", LocalDateTime.class)))
            .build();
    }

    /**
     * 查询订单、用户及订单项 - 展示多表 JOIN
     */
    public JooqOrderDto findOrderWithUserAndItemsById(Long orderId) {
        // 查询订单和用户信息
        Record record = dsl.select(
                field(ORDERS_TABLE + ".id"),
                field(ORDERS_TABLE + ".order_number"),
                field(ORDERS_TABLE + ".user_id"),
                field(ORDERS_TABLE + ".total_amount"),
                field(ORDERS_TABLE + ".status"),
                field(ORDERS_TABLE + ".remarks"),
                field(ORDERS_TABLE + ".created_at"),
                field(ORDERS_TABLE + ".updated_at"),
                field(USERS_TABLE + ".username").as("username")
            )
            .from(table(ORDERS_TABLE))
            .join(table(USERS_TABLE))
            .on(field(ORDERS_TABLE + ".user_id").eq(field(USERS_TABLE + ".id")))
            .where(field(ORDERS_TABLE + ".id").eq(orderId))
            .fetchOne();

        if (record == null) {
            return null;
        }

        JooqOrderDto order = JooqOrderDto.builder()
            .id(record.get(field("id", Long.class)))
            .orderNumber(record.get(field("order_number", String.class)))
            .userId(record.get(field("user_id", Long.class)))
            .username(record.get(field("username", String.class)))
            .totalAmount(record.get(field("total_amount", BigDecimal.class)))
            .status(record.get(field("status", String.class)))
            .remarks(record.get(field("remarks", String.class)))
            .createdAt(record.get(field("created_at", LocalDateTime.class)))
            .updatedAt(record.get(field("updated_at", LocalDateTime.class)))
            .build();

        // 查询订单项（关联商品表获取商品名称）
        Result<Record3<Long, Long, String>> itemRecords = dsl.select(
                field(ORDER_ITEMS_TABLE + ".id"),
                field(ORDER_ITEMS_TABLE + ".product_id"),
                field(PRODUCTS_TABLE + ".name").as("product_name"),
                field(ORDER_ITEMS_TABLE + ".quantity"),
                field(ORDER_ITEMS_TABLE + ".price"),
                field(ORDER_ITEMS_TABLE + ".subtotal")
            )
            .from(table(ORDER_ITEMS_TABLE))
            .leftJoin(table(PRODUCTS_TABLE))
            .on(field(ORDER_ITEMS_TABLE + ".product_id").eq(field(PRODUCTS_TABLE + ".id")))
            .where(field(ORDER_ITEMS_TABLE + ".order_id").eq(orderId))
            .fetch();

        List<JooqOrderItemDto> items = new ArrayList<>();
        for (var r : itemRecords) {
            items.add(JooqOrderItemDto.builder()
                .id(r.value1())
                .productId(r.value2())
                .productName(r.value3())
                .quantity(r.get(field("quantity", Integer.class)))
                .price(r.get(field("price", BigDecimal.class)))
                .subtotal(r.get(field("subtotal", BigDecimal.class)))
                .build());
        }

        order.setItems(items);
        return order;
    }

    /**
     * 根据用户ID查询订单列表
     */
    public List<JooqOrderDto> findOrdersByUserId(Long userId) {
        Result<Record> records = dsl.selectFrom(table(ORDERS_TABLE))
            .where(field("user_id").eq(userId))
            .orderBy(field("created_at").desc())
            .fetch();

        return records.map(this::mapToOrderDto);
    }

    /**
     * 订单统计 - 展示 GROUP BY 聚合查询
     */
    public Map<String, Object> getOrderStatistics() {
        // 统计各状态订单数量
        Result<Record2<String, Integer>> statusCounts = dsl.select(
                field("status"),
                count().as("count")
            )
            .from(table(ORDERS_TABLE))
            .groupBy(field("status"))
            .fetch();

        Map<String, Integer> statusMap = new HashMap<>();
        for (var record : statusCounts) {
            statusMap.put(
                record.get(field("status", String.class)),
                record.get(field("count", Integer.class))
            );
        }

        // 统计总金额
        Record1<BigDecimal> totalAmountRecord = dsl.select(
                sum(field("total_amount", BigDecimal.class))
            )
            .from(table(ORDERS_TABLE))
            .fetchOne();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_orders", statusMap.values().stream().mapToInt(Integer::intValue).sum());
        stats.put("total_amount", totalAmountRecord.component1() != null ? totalAmountRecord.component1() : BigDecimal.ZERO);
        stats.put("pending_orders", statusMap.getOrDefault("PENDING", 0));
        stats.put("shipped_orders", statusMap.getOrDefault("SHIPPED", 0));

        return stats;
    }

    private JooqOrderDto mapToOrderDto(Record record) {
        return JooqOrderDto.builder()
            .id(record.get(field("id", Long.class)))
            .orderNumber(record.get(field("order_number", String.class)))
            .userId(record.get(field("user_id", Long.class)))
            .totalAmount(record.get(field("total_amount", BigDecimal.class)))
            .status(record.get(field("status", String.class)))
            .remarks(record.get(field("remarks", String.class)))
            .createdAt(record.get(field("created_at", LocalDateTime.class)))
            .updatedAt(record.get(field("updated_at", LocalDateTime.class)))
            .build();
    }

    private JooqOrderItemDto mapToOrderItemDto(Record record) {
        return JooqOrderItemDto.builder()
            .id(record.get(field("id", Long.class)))
            .orderId(record.get(field("order_id", Long.class)))
            .productId(record.get(field("product_id", Long.class)))
            .quantity(record.get(field("quantity", Integer.class)))
            .price(record.get(field("price", BigDecimal.class)))
            .subtotal(record.get(field("subtotal", BigDecimal.class)))
            .build();
    }
}
```

**Step 4: 运行测试验证通过**

运行: `mvn test -Dtest=JooqOrderRepositoryTest`

预期: PASS - 所有测试通过

**Step 5: 提交**

```bash
git add src/test/java/com/example/demo/repository/jooq/JooqOrderRepositoryTest.java
git add src/main/java/com/example/demo/repository/jooq/JooqOrderRepository.java
git commit -m "feat(jooq): implement OrderRepository with complex queries"
```

---

### Task 6: 创建 Service 层 - 业务逻辑

**文件:**
- 创建: `src/main/java/com/example/demo/service/jooq/JooqUserService.java`
- 创建: `src/main/java/com/example/demo/service/jooq/JooqProductService.java`
- 创建: `src/main/java/com/example/demo/service/jooq/JooqOrderService.java`
- 创建: `src/main/java/com/example/demo/service/jooq/JooqTransactionService.java`

**Step 1: 实现 JooqUserService**

```java
package com.example.demo.service.jooq;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * JOOQ 用户服务
 *
 * @author chinwe
 */
@Service
@RequiredArgsConstructor
public class JooqUserService {

    private final JooqUserRepository userRepository;

    public Long createUser(JooqUserDto user) {
        return userRepository.insert(user);
    }

    public JooqUserDto getUserById(Long id) {
        return userRepository.findById(id);
    }

    public JooqUserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<JooqUserDto> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean updateUser(JooqUserDto user) {
        return userRepository.update(user);
    }

    public boolean deleteUser(Long id) {
        return userRepository.delete(id);
    }
}
```

**Step 2: 实现 JooqProductService**

```java
package com.example.demo.service.jooq;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.repository.jooq.JooqProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * JOOQ 商品服务
 *
 * @author chinwe
 */
@Service
@RequiredArgsConstructor
public class JooqProductService {

    private final JooqProductRepository productRepository;

    public Long createProduct(JooqCreateProductRequest request) {
        JooqProductDto product = JooqProductDto.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .category(request.getCategory())
            .build();
        return productRepository.insert(product);
    }

    public JooqProductDto getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void batchCreateProducts(List<JooqCreateProductRequest> requests) {
        List<JooqProductDto> products = requests.stream()
            .map(r -> JooqProductDto.builder()
                .name(r.getName())
                .description(r.getDescription())
                .price(r.getPrice())
                .stock(r.getStock())
                .category(r.getCategory())
                .build())
            .toList();
        productRepository.batchInsert(products);
    }

    public List<JooqProductDto> getProductsByCategory(String category, int page, int size) {
        return productRepository.findByCategory(category, page * size, size);
    }

    public boolean updateProduct(JooqProductDto product) {
        return productRepository.upsert(product);
    }

    public int getTotalStockByCategory(String category) {
        return productRepository.getTotalStockByCategory(category);
    }

    public List<JooqProductDto> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }
}
```

**Step 3: 实现 JooqOrderService**

```java
package com.example.demo.service.jooq;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderQueryRequest;
import com.example.demo.repository.jooq.JooqOrderRepository;

import lombok.RequiredArgsConstructor;

/**
 * JOOQ 订单服务
 *
 * @author chinwe
 */
@Service
@RequiredArgsConstructor
public class JooqOrderService {

    private final JooqOrderRepository orderRepository;

    public Long createOrder(JooqOrderDto order) {
        return orderRepository.insertWithItems(order);
    }

    public JooqOrderDto getOrderById(Long orderId) {
        return orderRepository.findOrderWithUserAndItemsById(orderId);
    }

    public List<JooqOrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    public java.util.Map<String, Object> getOrderStatistics() {
        return orderRepository.getOrderStatistics();
    }
}
```

**Step 4: 实现 JooqTransactionService（事务管理）**

```java
package com.example.demo.service.jooq;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqCreateOrderRequest;
import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqProductRepository;
import com.example.demo.repository.jooq.JooqUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JOOQ 事务服务
 * 展示声明式事务管理
 *
 * @author chinwe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JooqTransactionService {

    private final JooqUserRepository userRepository;
    private final JooqProductRepository productRepository;
    private final JooqOrderRepository orderRepository;

    /**
     * 创建订单 - 事务示例
     * 包含：验证用户、检查库存、扣减库存、创建订单、创建订单项
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(JooqCreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // 1. 验证用户存在
        JooqUserDto user = userRepository.findById(request.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + request.getUserId());
        }

        // 2. 计算总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        var orderItems = request.getItems().stream()
            .map(itemReq -> {
                JooqProductDto product = productRepository.findById(itemReq.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("商品不存在: " + itemReq.getProductId());
                }
                if (product.getStock() < itemReq.getQuantity()) {
                    throw new IllegalArgumentException("商品库存不足: " + product.getName());
                }

                BigDecimal subtotal = product.getPrice().multiply(
                    BigDecimal.valueOf(itemReq.getQuantity())
                );
                totalAmount = totalAmount.add(subtotal);

                return JooqOrderItemDto.builder()
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            })
            .toList();

        // 3. 扣减库存
        for (var item : request.getItems()) {
            boolean decreased = productRepository.decreaseStock(
                item.getProductId(),
                item.getQuantity()
            );
            if (!decreased) {
                throw new IllegalStateException("扣减库存失败: " + item.getProductId());
            }
        }

        // 4. 创建订单
        String orderNumber = generateOrderNumber();
        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber(orderNumber)
            .userId(request.getUserId())
            .totalAmount(totalAmount)
            .status("PENDING")
            .remarks(request.getRemarks())
            .items(orderItems)
            .build();

        Long orderId = orderRepository.insertWithItems(order);

        log.info("Order created successfully: {}", orderId);
        return orderId;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
```

**Step 5: 验证编译**

运行: `mvn clean compile`

预期: 编译成功

**Step 6: 提交**

```bash
git add src/main/java/com/example/demo/service/jooq/
git commit -m "feat(jooq): implement service layer with transaction management"
```

---

### Task 7: 创建 Controller 层

**文件:**
- 创建: `src/main/java/com/example/demo/controller/jooq/JooqController.java`

**Step 1: 创建 Controller**

```java
package com.example.demo.controller.jooq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.jooq.JooqCreateOrderRequest;
import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.service.jooq.JooqOrderService;
import com.example.demo.service.jooq.JooqProductService;
import com.example.demo.service.jooq.JooqTransactionService;
import com.example.demo.service.jooq.JooqUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * JOOQ 示例控制器
 * 展示各种 JOOQ 功能的 API 接口
 *
 * @author chinwe
 */
@Tag(name = "JOOQ 示例", description = "JOOQ 电商订单系统示例接口")
@RestController
@RequestMapping("/api/jooq")
@RequiredArgsConstructor
public class JooqController {

    private final JooqUserService userService;
    private final JooqProductService productService;
    private final JooqOrderService orderService;
    private final JooqTransactionService transactionService;

    // ==================== 用户相关接口 ====================

    @Operation(summary = "创建用户", description = "演示基础插入操作")
    @PostMapping("/users")
    public String createUser(@RequestBody JooqUserDto user) {
        Long id = userService.createUser(user);
        return "用户创建成功，ID: " + id;
    }

    @Operation(summary = "查询用户", description = "演示基础查询操作")
    @GetMapping("/users/{id}")
    public JooqUserDto getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "查询所有用户", description = "演示列表查询操作")
    @GetMapping("/users")
    public List<JooqUserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "根据用户名查询", description = "演示条件查询操作")
    @GetMapping("/users/username/{username}")
    public JooqUserDto getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    // ==================== 商品相关接口 ====================

    @Operation(summary = "创建商品", description = "演示单条插入操作")
    @PostMapping("/products")
    public String createProduct(@Valid @RequestBody JooqCreateProductRequest request) {
        Long id = productService.createProduct(request);
        return "商品创建成功，ID: " + id;
    }

    @Operation(summary = "批量创建商品", description = "演示批量插入操作")
    @PostMapping("/products/batch")
    public String batchCreateProducts(@RequestBody List<JooqCreateProductRequest> requests) {
        productService.batchCreateProducts(requests);
        return "批量创建商品成功，数量: " + requests.size();
    }

    @Operation(summary = "查询商品", description = "演示基础查询操作")
    @GetMapping("/products/{id}")
    public JooqProductDto getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @Operation(summary = "按分类查询商品", description = "演示分页查询操作")
    @GetMapping("/products")
    public List<JooqProductDto> getProductsByCategory(
        @RequestParam String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return productService.getProductsByCategory(category, page, size);
    }

    @Operation(summary = "查询低库存商品", description = "演示复杂条件查询操作")
    @GetMapping("/products/low-stock")
    public List<JooqProductDto> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return productService.getLowStockProducts(threshold);
    }

    @Operation(summary = "查询分类库存统计", description = "演示聚合查询操作")
    @GetMapping("/products/stock/{category}")
    public String getTotalStock(@PathVariable String category) {
        int total = productService.getTotalStockByCategory(category);
        return "分类 [" + category + "] 总库存: " + total;
    }

    // ==================== 订单相关接口 ====================

    @Operation(summary = "创建订单", description = "演示事务管理操作（验证用户、扣减库存、创建订单）")
    @PostMapping("/orders")
    public String createOrder(@Valid @RequestBody JooqCreateOrderRequest request) {
        Long orderId = transactionService.createOrder(request);
        return "订单创建成功，订单ID: " + orderId;
    }

    @Operation(summary = "查询订单详情", description = "演示多表 JOIN 查询操作")
    @GetMapping("/orders/{id}")
    public JooqOrderDto getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @Operation(summary = "查询用户订单", description = "演示关联查询操作")
    @GetMapping("/users/{userId}/orders")
    public List<JooqOrderDto> getUserOrders(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @Operation(summary = "订单统计", description = "演示 GROUP BY 聚合查询操作")
    @GetMapping("/orders/statistics")
    public Map<String, Object> getOrderStatistics() {
        return orderService.getOrderStatistics();
    }
}
```

**Step 2: 验证编译**

运行: `mvn clean compile`

预期: 编译成功

**Step 3: 提交**

```bash
git add src/main/java/com/example/demo/controller/jooq/JooqController.java
git commit -m "feat(jooq): add REST API controller"
```

---

### Task 8: 集成测试

**文件:**
- 创建: `src/test/java/com/example/demo/controller/jooq/JooqControllerTest.java`
- 创建: `src/test/java/com/example/demo/integration/JooqIntegrationTest.java`

**Step 1: 创建 Controller 测试**

创建: `src/test/java/com/example/demo/controller/jooq/JooqControllerTest.java`

```java
package com.example.demo.controller.jooq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.service.jooq.JooqOrderService;
import com.example.demo.service.jooq.JooqProductService;
import com.example.demo.service.jooq.JooqTransactionService;
import com.example.demo.service.jooq.JooqUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JooqController 测试类
 *
 * @author chinwe
 */
@WebMvcTest(JooqController.class)
class JooqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JooqUserService userService;

    @MockBean
    private JooqProductService productService;

    @MockBean
    private JooqOrderService orderService;

    @MockBean
    private JooqTransactionService transactionService;

    @Test
    void testGetUser() throws Exception {
        // Given
        JooqUserDto user = JooqUserDto.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        when(userService.getUserById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/jooq/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetProduct() throws Exception {
        // Given
        JooqProductDto product = JooqProductDto.builder()
            .id(1L)
            .name("Product 1")
            .price(new BigDecimal("100.00"))
            .stock(10)
            .build();
        when(productService.getProductById(1L)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/jooq/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Product 1"))
            .andExpect(jsonPath("$.price").value(100.00));
    }

    @Test
    void testGetOrder() throws Exception {
        // Given
        JooqOrderDto order = JooqOrderDto.builder()
            .id(1L)
            .orderNumber("ORD001")
            .userId(1L)
            .username("testuser")
            .totalAmount(new BigDecimal("100.00"))
            .status("PENDING")
            .build();
        when(orderService.getOrderById(1L)).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/jooq/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNumber").value("ORD001"))
            .andExpect(jsonPath("$.username").value("testuser"));
    }
}
```

**Step 2: 创建集成测试**

创建: `src/test/java/com/example/demo/integration/JooqIntegrationTest.java`

```java
package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqCreateOrderRequest;
import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqProductRepository;
import com.example.demo.repository.jooq.JooqUserRepository;
import com.example.demo.service.jooq.JooqTransactionService;

/**
 * JOOQ 集成测试
 * 测试完整的事务流程
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private JooqTransactionService transactionService;

    private JooqUserRepository userRepository;
    private JooqProductRepository productRepository;
    private JooqOrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        userRepository = new JooqUserRepository(dsl);
        productRepository = new JooqProductRepository(dsl);
        orderRepository = new JooqOrderRepository(dsl);
    }

    @Test
    void testCompleteOrderFlow() {
        // 1. 创建用户
        JooqUserDto user = JooqUserDto.builder()
            .username("john")
            .email("john@example.com")
            .build();
        Long userId = userRepository.insert(user);
        assertNotNull(userId);

        // 2. 创建商品
        List<JooqCreateProductRequest> products = List.of(
            new JooqCreateProductRequest("Laptop", "High-end laptop", new BigDecimal("1000.00"), 10, "Electronics"),
            new JooqCreateProductRequest("Mouse", "Wireless mouse", new BigDecimal("20.00"), 50, "Electronics")
        );
        for (var p : products) {
            Long productId = productRepository.insert(
                JooqProductDto.builder()
                    .name(p.getName())
                    .description(p.getDescription())
                    .price(p.getPrice())
                    .stock(p.getStock())
                    .category(p.getCategory())
                    .build()
            );
            assertNotNull(productId);
        }

        // 3. 查询商品获取ID
        List<JooqProductDto> allProducts = productRepository.findByCategory("Electronics", 0, 10);
        assertEquals(2, allProducts.size());

        // 4. 创建订单
        JooqCreateOrderRequest orderRequest = new JooqCreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(allProducts.get(0).getId(), 1),
            new JooqCreateOrderRequest.OrderItemRequest(allProducts.get(1).getId(), 2)
        ));
        orderRequest.setRemarks("Please ship quickly");

        Long orderId = transactionService.createOrder(orderRequest);
        assertNotNull(orderId);

        // 5. 验证订单创建
        JooqOrderDto order = orderRepository.findOrderWithUserAndItemsById(orderId);
        assertNotNull(order);
        assertEquals(userId, order.getUserId());
        assertEquals("john", order.getUsername());
        assertEquals(new BigDecimal("1040.00"), order.getTotalAmount());
        assertNotNull(order.getItems());
        assertEquals(2, order.getItems().size());

        // 6. 验证库存扣减
        JooqProductDto laptop = productRepository.findById(allProducts.get(0).getId());
        JooqProductDto mouse = productRepository.findById(allProducts.get(1).getId());
        assertEquals(9, laptop.getStock()); // 原来是10，买了1个
        assertEquals(48, mouse.getStock()); // 原来是50，买了2个
    }

    @Test
    void testTransactionRollback() {
        // Given
        Long userId = userRepository.insert(
            JooqUserDto.builder()
                .username("john")
                .email("john@example.com")
                .build()
        );

        // 商品库存只有5个
        Long productId = productRepository.insert(
            JooqProductDto.builder()
                .name("Product")
                .price(new BigDecimal("100.00"))
                .stock(5)
                .build()
        );

        // When - 尝试购买10个，应该失败
        JooqCreateOrderRequest orderRequest = new JooqCreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setItems(List.of(
            new JooqCreateOrderRequest.OrderItemRequest(productId, 10)
        ));

        // Then - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createOrder(orderRequest);
        });

        // 验证库存没有被扣减（事务回滚）
        JooqProductDto product = productRepository.findById(productId);
        assertEquals(5, product.getStock());
    }
}
```

**Step 3: 运行测试**

运行: `mvn test -Dtest=JooqControllerTest`

运行: `mvn test -Dtest=JooqIntegrationTest`

预期: PASS - 所有测试通过

**Step 4: 提交**

```bash
git add src/test/java/com/example/demo/controller/jooq/JooqControllerTest.java
git add src/test/java/com/example/demo/integration/JooqIntegrationTest.java
git commit -m "test(jooq): add controller and integration tests"
```

---

### Task 9: 更新文档

**文件:**
- 修改: `README.md`
- 修改: `CLAUDE.md`

**Step 1: 更新 README.md**

在 README.md 中添加 JOOQ 示例的 API 文档：

```markdown
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
```

**Step 2: 更新 CLAUDE.md**

在 CLAUDE.md 的架构部分添加 JOOQ 模块说明：

```markdown
### JOOQ 模块
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
```

**Step 3: 验证**

运行: `mvn clean test`

预期: 所有测试通过

**Step 4: 提交**

```bash
git add README.md CLAUDE.md
git commit -m "docs(jooq): update documentation for JOOQ examples"
```

---

### Task 10: 最终验证

**Step 1: 运行所有测试**

运行: `mvn clean test`

预期: 所有测试通过

**Step 2: 编译检查**

运行: `mvn clean compile`

预期: 编译成功

**Step 3: 启动应用验证**

运行: `mvn spring-boot:run`

访问: `http://localhost:8080/swagger-ui.html`

预期: 看到 JOOQ 相关的 API 接口

**Step 4: 提交最终版本**

```bash
git add .
git commit -m "feat(jooq): complete JOOQ e-commerce examples implementation"
```

---

## 实施总结

完成后，项目将包含：

1. **完整的数据库表结构** - 4个表（用户、商品、订单、订单项）
2. **Repository 层** - 展示各种 JOOQ 查询技巧
3. **Service 层** - 业务逻辑和事务管理
4. **Controller 层** - REST API 接口
5. **完整的测试覆盖** - 单元测试和集成测试
6. **API 文档** - Swagger UI 集成

## 关键技术点

- **类型安全查询**: 使用 DSL 生成 SQL
- **JOIN 查询**: 多表关联查询
- **聚合函数**: SUM、COUNT、GROUP BY
- **批量操作**: batch insert
- **Upsert**: MERGE 语法
- **事务管理**: @Transactional 注解
- **分页查询**: LIMIT 和 OFFSET
- **条件更新**: WHERE 条件与原子操作

## 注意事项

1. 所有代码遵循项目规范：代码使用英文，注释使用中文
2. 使用 TDD 开发模式：先写测试，再写实现
3. 每个任务完成后提交代码
4. 所有测试必须通过才能进入下一个任务
