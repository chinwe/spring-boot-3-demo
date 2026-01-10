package com.example.demo.service.orika;

import java.time.ZoneOffset;
import java.util.List;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import ma.glasnost.orika.MapperFacade;

import org.springframework.stereotype.Service;

import com.example.demo.dto.OrderDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;

/**
 * Orika 映射服务
 *
 * 功能特性：
 * 1. 封装 Orika 映射操作
 * 2. 提供便捷的映射方法
 * 3. 展示 Orika 核心特性
 *
 * @author chinwe
 */
@Slf4j
@Service
public class OrikaMappingService {

    /**
     * Orika MapperFacade（线程安全，可并发使用）
     */
    @Resource
    private MapperFacade mapperFacade;

    /**
     * 基础对象映射：User -> UserDto
     *
     * 展示 Orika 的基础映射功能：
     * - 同名字段自动映射
     * - 类型安全保证
     *
     * @param user 源 User 对象
     * @return 映射后的 UserDto 对象
     */
    public UserDto toUserDto(User user) {
        if (user == null) {
            log.warn("User object is null, returning null");
            return null;
        }

        log.debug("Mapping User to UserDto: id={}, name={}", user.getId(), user.getName());
        UserDto dto = mapperFacade.map(user, UserDto.class);
        log.debug("Mapped UserDto: name={}, email={}", dto.getName(), dto.getEmail());

        return dto;
    }

    /**
     * 反向映射：UserDto -> User
     *
     * @param dto 源 UserDto 对象
     * @return 映射后的 User 对象
     */
    public User toUser(UserDto dto) {
        if (dto == null) {
            log.warn("UserDto object is null, returning null");
            return null;
        }

        log.debug("Mapping UserDto to User: name={}", dto.getName());
        User user = mapperFacade.map(dto, User.class);
        log.debug("Mapped User: id={}, name={}", user.getId(), user.getName());

        return user;
    }

    /**
     * 复杂对象映射：Order -> OrderDto
     *
     * 展示 Orika 的高级映射功能：
     * - 嵌套对象映射（customer.fullName -> customerName）
     * - 自定义转换器（LocalDateTime -> String, Enum -> String）
     * - 字段重命名
     *
     * @param order 源 Order 对象
     * @return 映射后的 OrderDto 对象
     */
    public OrderDto toOrderDto(Order order) {
        if (order == null) {
            log.warn("Order object is null, returning null");
            return null;
        }

        log.debug("Mapping Order to OrderDto: id={}, orderNumber={}", order.getId(), order.getOrderNumber());
        OrderDto dto = mapperFacade.map(order, OrderDto.class);

        // 设置标识字段，区分映射工具
        dto.setMappedBy("Orika");

        // 计算并设置时间戳
        if (order.getCreatedAt() != null) {
            dto.setCreatedAtEpoch(order.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
        }

        // 计算校验和
        if (order.getOrderNumber() != null) {
            dto.setChecksum(calculateChecksum(order.getOrderNumber()));
        }

        log.debug("Mapped OrderDto: orderNumber={}, customerName={}, mappedBy={}",
            dto.getOrderNumber(), dto.getCustomerName(), dto.getMappedBy());

        return dto;
    }

    /**
     * 集合映射：List<Order> -> List<OrderDto>
     *
     * 展示 Orika 的集合映射功能
     *
     * @param orders 源 Order 列表
     * @return 映射后的 OrderDto 列表
     */
    public List<OrderDto> toOrderDtoList(List<Order> orders) {
        if (orders == null) {
            log.warn("Order list is null, returning null");
            return null;
        }

        log.debug("Mapping Order list to OrderDto list: size={}", orders.size());

        // 使用 Orika 的 mapAsList 方法进行集合映射
        List<OrderDto> dtoList = mapperFacade.mapAsList(orders, OrderDto.class);

        // 为每个 DTO 设置标识字段和计算字段
        dtoList.forEach(dto -> {
            dto.setMappedBy("Orika");
            if (dto.getOrderNumber() != null) {
                dto.setChecksum(calculateChecksum(dto.getOrderNumber()));
            }
        });

        log.debug("Mapped {} OrderDto objects", dtoList.size());

        return dtoList;
    }

    /**
     * 批量映射（性能优化版本）
     *
     * 使用并行流处理大量数据
     *
     * @param orders 源 Order 列表
     * @return 映射后的 OrderDto 列表
     */
    public List<OrderDto> toOrderDtoListBatch(List<Order> orders) {
        if (orders == null) {
            log.warn("Order list is null, returning null");
            return null;
        }

        log.debug("Batch mapping Order list: size={}", orders.size());

        // 使用并行流提高性能
        List<OrderDto> dtoList = orders.parallelStream()
            .map(this::toOrderDto)
            .toList();

        log.debug("Batch mapped {} OrderDto objects", dtoList.size());

        return dtoList;
    }

    /**
     * 计算字符串的校验和
     *
     * @param input 输入字符串
     * @return 校验和的十六进制表示
     */
    private String calculateChecksum(String input) {
        if (input == null) {
            return null;
        }
        return Integer.toHexString(input.hashCode());
    }
}
