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
