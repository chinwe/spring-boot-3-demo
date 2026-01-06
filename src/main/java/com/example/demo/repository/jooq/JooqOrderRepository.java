package com.example.demo.repository.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record10;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.jooq.JooqOrderDto;
import com.example.demo.dto.jooq.JooqOrderItemDto;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

/**
 * JOOQ 订单仓库
 * 展示复杂查询：多表操作、JOIN、GROUP BY 聚合
 *
 * @author chinwe
 */
@Repository
public class JooqOrderRepository {

    private final DSLContext dsl;

    // 订单表定义
    private static final String ORDERS_TABLE = "j_orders";
    private static final Field<Long> ORDER_ID = field("id", BIGINT);
    private static final Field<String> ORDER_NUMBER = field("order_number", VARCHAR);
    private static final Field<Long> USER_ID = field("user_id", BIGINT);
    private static final Field<BigDecimal> TOTAL_AMOUNT = field("total_amount", NUMERIC);
    private static final Field<String> STATUS = field("status", VARCHAR);
    private static final Field<String> REMARKS = field("remarks", VARCHAR);
    private static final Field<LocalDateTime> ORDER_CREATED_AT = field("created_at", LOCALDATETIME);
    private static final Field<LocalDateTime> ORDER_UPDATED_AT = field("updated_at", LOCALDATETIME);

    // 订单项表定义
    private static final String ORDER_ITEMS_TABLE = "j_order_items";
    private static final Field<Long> ITEM_ID = field("id", BIGINT);
    private static final Field<Long> ITEM_ORDER_ID = field("order_id", BIGINT);
    private static final Field<Long> ITEM_PRODUCT_ID = field("product_id", BIGINT);
    private static final Field<Integer> QUANTITY = field("quantity", INTEGER);
    private static final Field<BigDecimal> PRICE = field("price", NUMERIC);
    private static final Field<BigDecimal> SUBTOTAL = field("subtotal", NUMERIC);

    // 用户表定义
    private static final String USERS_TABLE = "j_users";
    private static final Field<Long> USER_ID_FIELD = field("id", BIGINT);
    private static final Field<String> USERNAME = field("username", VARCHAR);

    public JooqOrderRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 插入订单及其订单项（多表操作）
     *
     * @param order 订单 DTO
     * @return 生成的订单 ID
     * @throws IllegalArgumentException 如果 order 为 null
     */
    public Long insertWithItems(JooqOrderDto order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        // Insert order
        Record orderRecord = dsl.insertInto(
                table(ORDERS_TABLE),
                ORDER_NUMBER, USER_ID, TOTAL_AMOUNT, STATUS, REMARKS, ORDER_CREATED_AT, ORDER_UPDATED_AT
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
            .returning(ORDER_ID)
            .fetchOne();

        Long orderId = orderRecord.get(ORDER_ID);

        // Insert order items
        List<JooqOrderItemDto> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            for (JooqOrderItemDto item : items) {
                dsl.insertInto(
                        table(ORDER_ITEMS_TABLE),
                        ITEM_ORDER_ID, ITEM_PRODUCT_ID, QUANTITY, PRICE, SUBTOTAL
                    )
                    .values(
                        orderId,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                    )
                    .execute();
            }
        }

        return orderId;
    }

    /**
     * 查询订单及其订单项（一对多查询）
     *
     * @param orderId 订单 ID
     * @return 订单 DTO，不存在返回 null
     */
    public JooqOrderDto findOrderWithItemsById(Long orderId) {
        // Query order
        Record orderRecord = dsl.select(
                ORDER_ID, ORDER_NUMBER, USER_ID, TOTAL_AMOUNT, STATUS, REMARKS, ORDER_CREATED_AT, ORDER_UPDATED_AT
            )
            .from(table(ORDERS_TABLE))
            .where(ORDER_ID.eq(orderId))
            .fetchOne();

        if (orderRecord == null) {
            return null;
        }

        // Query order items
        Result<Record> itemRecords = dsl.select(
                ITEM_ID, ITEM_ORDER_ID, ITEM_PRODUCT_ID, QUANTITY, PRICE, SUBTOTAL
            )
            .from(table(ORDER_ITEMS_TABLE))
            .where(ITEM_ORDER_ID.eq(orderId))
            .fetch();

        List<JooqOrderItemDto> items = itemRecords.map(r -> JooqOrderItemDto.builder()
            .id(r.get(ITEM_ID))
            .orderId(r.get(ITEM_ORDER_ID))
            .productId(r.get(ITEM_PRODUCT_ID))
            .quantity(r.get(QUANTITY))
            .price(r.get(PRICE))
            .subtotal(r.get(SUBTOTAL))
            .build());

        return mapToOrderDto(orderRecord, items);
    }

    /**
     * 查询订单及用户信息（INNER JOIN）
     *
     * @param orderId 订单 ID
     * @return 订单 DTO，不存在返回 null
     */
    public JooqOrderDto findOrderWithUserById(Long orderId) {
        Record4<Long, String, Long, String> record = dsl.select(
                ORDER_ID,
                ORDER_NUMBER,
                USER_ID,
                USERNAME
            )
            .from(table(ORDERS_TABLE))
            .join(table(USERS_TABLE))
            .on(USER_ID.eq(USER_ID_FIELD))
            .where(ORDER_ID.eq(orderId))
            .fetchOne();

        if (record == null) {
            return null;
        }

        return JooqOrderDto.builder()
            .id(record.get(ORDER_ID))
            .orderNumber(record.get(ORDER_NUMBER))
            .userId(record.get(USER_ID))
            .username(record.get(USERNAME))
            .items(List.of())
            .build();
    }

    /**
     * 查询订单、用户及订单项（多表 LEFT JOIN）
     *
     * @param orderId 订单 ID
     * @return 订单 DTO，不存在返回 null
     */
    public JooqOrderDto findOrderWithUserAndItemsById(Long orderId) {
        // Query order with user
        Record orderRecord = dsl.select(
                ORDER_ID,
                ORDER_NUMBER,
                table(ORDERS_TABLE).field(USER_ID),
                TOTAL_AMOUNT,
                STATUS,
                REMARKS,
                ORDER_CREATED_AT,
                ORDER_UPDATED_AT,
                USERNAME
            )
            .from(table(ORDERS_TABLE))
            .leftJoin(table(USERS_TABLE))
            .on(USER_ID.eq(USER_ID_FIELD))
            .where(ORDER_ID.eq(orderId))
            .fetchOne();

        if (orderRecord == null) {
            return null;
        }

        // Query order items
        Result<Record> itemRecords = dsl.select(
                ITEM_ID, ITEM_ORDER_ID, ITEM_PRODUCT_ID, QUANTITY, PRICE, SUBTOTAL
            )
            .from(table(ORDER_ITEMS_TABLE))
            .where(ITEM_ORDER_ID.eq(orderId))
            .fetch();

        List<JooqOrderItemDto> items = itemRecords.map(r -> JooqOrderItemDto.builder()
            .id(r.get(ITEM_ID))
            .orderId(r.get(ITEM_ORDER_ID))
            .productId(r.get(ITEM_PRODUCT_ID))
            .quantity(r.get(QUANTITY))
            .price(r.get(PRICE))
            .subtotal(r.get(SUBTOTAL))
            .build());

        return JooqOrderDto.builder()
            .id(orderRecord.get(ORDER_ID))
            .orderNumber(orderRecord.get(ORDER_NUMBER))
            .userId(orderRecord.get(USER_ID))
            .username(orderRecord.get(USERNAME))
            .totalAmount(orderRecord.get(TOTAL_AMOUNT))
            .status(orderRecord.get(STATUS))
            .remarks(orderRecord.get(REMARKS))
            .createdAt(orderRecord.get(ORDER_CREATED_AT))
            .updatedAt(orderRecord.get(ORDER_UPDATED_AT))
            .items(items)
            .build();
    }

    /**
     * 查询用户的所有订单（关联查询）
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    public List<JooqOrderDto> findOrdersByUserId(Long userId) {
        Result<Record10<Long, String, Long, BigDecimal, String, String, LocalDateTime, LocalDateTime, String, Long>> records =
            dsl.select(
                    ORDER_ID,
                    ORDER_NUMBER,
                    table(ORDERS_TABLE).field(USER_ID),
                    TOTAL_AMOUNT,
                    STATUS,
                    REMARKS,
                    ORDER_CREATED_AT,
                    ORDER_UPDATED_AT,
                    USERNAME,
                    table(USERS_TABLE).field(USER_ID)
                )
                .from(table(ORDERS_TABLE))
                .leftJoin(table(USERS_TABLE))
                .on(USER_ID.eq(USER_ID_FIELD))
                .where(table(ORDERS_TABLE).field(USER_ID).eq(userId))
                .orderBy(ORDER_CREATED_AT.desc())
                .fetch();

        return records.map(r -> JooqOrderDto.builder()
            .id(r.get(ORDER_ID))
            .orderNumber(r.get(ORDER_NUMBER))
            .userId(r.getValue(table(ORDERS_TABLE).field(USER_ID), Long.class))
            .username(r.get(USERNAME))
            .totalAmount(r.get(TOTAL_AMOUNT))
            .status(r.get(STATUS))
            .remarks(r.get(REMARKS))
            .createdAt(r.get(ORDER_CREATED_AT))
            .updatedAt(r.get(ORDER_UPDATED_AT))
            .items(List.of())
            .build());
    }

    /**
     * 获取订单统计信息（GROUP BY 聚合）
     *
     * @return 统计信息 Map
     */
    public Map<String, Object> getOrderStatistics() {
        // Total orders and amount
        Record totalRecord = dsl.select(
                count().as("total_orders"),
                sum(TOTAL_AMOUNT).as("total_amount")
            )
            .from(table(ORDERS_TABLE))
            .fetchOne();

        // Count by status
        Result<Record> statusRecords = dsl.select(
                STATUS,
                count().as("count")
            )
            .from(table(ORDERS_TABLE))
            .groupBy(STATUS)
            .fetch();

        Map<String, Long> statusCounts = new HashMap<>();
        for (Record r : statusRecords) {
            String status = r.get(STATUS);
            Long count = r.get("count", Long.class);
            statusCounts.put(status != null ? status : "NULL", count);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_orders", totalRecord.get("total_orders", Long.class));
        stats.put("total_amount", totalRecord.get("total_amount", BigDecimal.class));
        stats.put("status_counts", statusCounts);

        return stats;
    }

    /**
     * 将 Record 映射为 OrderDto
     */
    private JooqOrderDto mapToOrderDto(Record record, List<JooqOrderItemDto> items) {
        return JooqOrderDto.builder()
            .id(record.get(ORDER_ID))
            .orderNumber(record.get(ORDER_NUMBER))
            .userId(record.get(USER_ID))
            .totalAmount(record.get(TOTAL_AMOUNT))
            .status(record.get(STATUS))
            .remarks(record.get(REMARKS))
            .createdAt(record.get(ORDER_CREATED_AT))
            .updatedAt(record.get(ORDER_UPDATED_AT))
            .items(items != null ? items : List.of())
            .build();
    }
}
