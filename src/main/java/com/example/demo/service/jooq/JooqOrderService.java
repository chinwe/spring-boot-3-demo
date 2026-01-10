package com.example.demo.service.jooq;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JOOQ 订单服务
 *
 * @author chinwe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JooqOrderService {

    private final JooqOrderRepository orderRepository;

    /**
     * 创建订单
     *
     * @param order 订单 DTO
     * @return 订单 ID
     */
    public Long createOrder(JooqOrderDto order) {
        return orderRepository.insertWithItems(order);
    }

    /**
     * 根据 ID 查询订单（包含用户和订单项）
     *
     * @param orderId 订单 ID
     * @return 订单 DTO
     * @throws EntityNotFoundException 订单不存在时抛出
     */
    public JooqOrderDto getOrderById(Long orderId) {
        log.debug("Fetching order by id: {}", orderId);
        JooqOrderDto order = orderRepository.findOrderWithUserAndItemsById(orderId);
        if (order == null) {
            log.warn("Order not found with id: {}", orderId);
            throw new EntityNotFoundException("Order not found with id: " + orderId);
        }
        return order;
    }

    /**
     * 查询用户的所有订单
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    public List<JooqOrderDto> getOrdersByUserId(Long userId) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findOrdersByUserId(userId);
    }

    /**
     * 获取订单统计信息
     *
     * @return 统计信息 Map
     */
    public Map<String, Object> getOrderStatistics() {
        return orderRepository.getOrderStatistics();
    }
}
