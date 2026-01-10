package com.example.demo.service.jooq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqCreateOrderRequest;
import com.example.demo.dto.jooq.JooqCreateOrderRequest.OrderItemRequest;
import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.dto.jooq.JooqUserDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqOrderRepository;
import com.example.demo.repository.jooq.JooqProductRepository;
import com.example.demo.repository.jooq.JooqUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JOOQ 事务服务
 * 展示事务管理
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
     * 创建订单（事务方法）
     *
     * @param request 创建订单请求
     * @return 订单 ID
     * @throws EntityNotFoundException 如果用户或商品不存在
     * @throws IllegalArgumentException 如果库存不足
     * @throws IllegalStateException 如果扣减库存失败
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(JooqCreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // 查询用户
        JooqUserDto user = userRepository.findById(request.getUserId());
        if (user == null) {
            throw new EntityNotFoundException("User not found with id: " + request.getUserId());
        }

        // 计算总金额并验证商品和库存
        final BigDecimal[] totalAmountHolder = new BigDecimal[]{BigDecimal.ZERO};
        List<JooqOrderItemDto> orderItems = request.getItems().stream()
            .map(itemReq -> {
                // 查询商品
                JooqProductDto product = productRepository.findById(itemReq.getProductId());
                if (product == null) {
                    throw new EntityNotFoundException("Product not found with id: " + itemReq.getProductId());
                }

                // 检查库存
                if (product.getStock() < itemReq.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }

                // 计算小计
                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmountHolder[0] = totalAmountHolder[0].add(subtotal);

                return JooqOrderItemDto.builder()
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            })
            .toList();

        // 扣减库存
        for (OrderItemRequest item : request.getItems()) {
            boolean decreased = productRepository.decreaseStock(item.getProductId(), item.getQuantity());
            if (!decreased) {
                throw new IllegalStateException("Failed to decrease stock for product: " + item.getProductId());
            }
        }

        // 生成订单号
        String orderNumber = "ORD" + System.currentTimeMillis() +
            UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建订单
        JooqOrderDto order = JooqOrderDto.builder()
            .orderNumber(orderNumber)
            .userId(request.getUserId())
            .totalAmount(totalAmountHolder[0])
            .status("PENDING")
            .remarks(request.getRemarks())
            .items(orderItems)
            .build();

        Long orderId = orderRepository.insertWithItems(order);
        log.info("Order created successfully: {}", orderId);
        return orderId;
    }

    /**
     * 创建订单并返回详细信息
     *
     * @param request 创建订单请求
     * @return 订单 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public JooqOrderDto createOrderWithDetails(JooqCreateOrderRequest request) {
        Long orderId = createOrder(request);
        return orderRepository.findOrderWithUserAndItemsById(orderId);
    }

    /**
     * 批量创建订单（事务方法）
     *
     * @param requests 创建订单请求列表
     * @return 订单 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateOrders(List<JooqCreateOrderRequest> requests) {
        return requests.stream()
            .map(this::createOrder)
            .toList();
    }

    /**
     * 获取系统统计信息
     *
     * @return 统计信息 Map
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> orderStats = orderRepository.getOrderStatistics();

        // 获取商品统计
        int electronicsStock = productRepository.getTotalStockByCategory("Electronics");
        int booksStock = productRepository.getTotalStockByCategory("Books");
        List<JooqProductDto> lowStockProducts = productRepository.findLowStockProducts(10);

        orderStats.put("electronics_stock", electronicsStock);
        orderStats.put("books_stock", booksStock);
        orderStats.put("low_stock_products_count", lowStockProducts.size());

        return orderStats;
    }
}
