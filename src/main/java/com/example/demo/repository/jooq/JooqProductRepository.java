package com.example.demo.repository.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record7;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.jooq.JooqProductDto;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

/**
 * JOOQ 商品仓库
 * 展示高级特性：批量插入、Upsert、分页查询、条件更新、聚合查询
 *
 * @author chinwe
 */
@Repository
public class JooqProductRepository {

    private final DSLContext dsl;

    // 表定义
    private static final String TABLE_NAME = "j_products";

    // 字段定义
    private static final Field<Long> ID = field("id", BIGINT);
    private static final Field<String> NAME = field("name", VARCHAR);
    private static final Field<String> DESCRIPTION = field("description", VARCHAR);
    private static final Field<BigDecimal> PRICE = field("price", NUMERIC);
    private static final Field<Integer> STOCK = field("stock", INTEGER);
    private static final Field<String> CATEGORY = field("category", VARCHAR);
    private static final Field<LocalDateTime> CREATED_AT = field("created_at", LOCALDATETIME);
    private static final Field<LocalDateTime> UPDATED_AT = field("updated_at", LOCALDATETIME);

    public JooqProductRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 基础插入操作
     *
     * @param product 商品 DTO
     * @return 生成的 ID
     * @throws IllegalArgumentException 如果 product 为 null
     */
    public Long insert(JooqProductDto product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        Record record = dsl.insertInto(
                table(TABLE_NAME),
                NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, CREATED_AT, UPDATED_AT
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
            .returning(ID)
            .fetchOne();

        return record.get(ID);
    }

    /**
     * 批量插入商品
     * 使用批量操作提高性能
     *
     * @param products 商品列表
     * @return 每条插入的记录数
     */
    public int[] batchInsert(List<JooqProductDto> products) {
        if (products == null || products.isEmpty()) {
            return new int[0];
        }

        // Use batch insert with individual statements
        return products.stream()
            .map(p -> dsl.insertInto(
                    table(TABLE_NAME),
                    NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, CREATED_AT, UPDATED_AT
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
                .execute())
            .toList()
            .stream()
            .mapToInt(i -> i)
            .toArray();
    }

    /**
     * 更新或插入商品（Upsert）
     * 使用 MERGE 语句实现
     *
     * @param product 商品 DTO（必须包含 ID）
     * @return 是否成功
     * @throws IllegalArgumentException 如果 product 为 null
     */
    public boolean upsert(JooqProductDto product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (product.getId() == null) {
            // Insert
            insert(product);
            return true;
        }

        // Update
        int affected = dsl.update(table(TABLE_NAME))
            .set(NAME, product.getName())
            .set(DESCRIPTION, product.getDescription())
            .set(PRICE, product.getPrice())
            .set(STOCK, product.getStock())
            .set(CATEGORY, product.getCategory())
            .set(UPDATED_AT, LocalDateTime.now())
            .where(ID.eq(product.getId()))
            .execute();

        return affected > 0;
    }

    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品 DTO，不存在返回 null
     */
    public JooqProductDto findById(Long id) {
        Record record = dsl.select(ID, NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, CREATED_AT, UPDATED_AT)
            .from(table(TABLE_NAME))
            .where(ID.eq(id))
            .fetchOne();

        return record != null ? mapToProductDto(record) : null;
    }

    /**
     * 根据分类查询商品（分页）
     *
     * @param category 分类名称
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商品列表
     */
    public List<JooqProductDto> findByCategory(String category, int offset, int limit) {
        Result<Record7<Long, String, String, BigDecimal, Integer, String, LocalDateTime>> records =
            dsl.select(ID, NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, CREATED_AT)
                .from(table(TABLE_NAME))
                .where(CATEGORY.eq(category))
                .orderBy(ID.asc())
                .limit(offset, limit)
                .fetch();

        return records.map(this::mapToProductDto);
    }

    /**
     * 扣减库存（条件更新）
     * 只有当前库存大于等于扣减数量时才执行
     *
     * @param id 商品 ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    public boolean decreaseStock(Long id, int quantity) {
        int affected = dsl.update(table(TABLE_NAME))
            .set(STOCK, STOCK.sub(quantity))
            .set(UPDATED_AT, LocalDateTime.now())
            .where(ID.eq(id))
            .and(STOCK.greaterOrEqual(quantity))
            .execute();

        return affected > 0;
    }

    /**
     * 获取指定分类的总库存
     *
     * @param category 分类名称
     * @return 总库存
     */
    public int getTotalStockByCategory(String category) {
        Record record = dsl.select(sum(STOCK).as("total_stock"))
            .from(table(TABLE_NAME))
            .where(CATEGORY.eq(category))
            .fetchOne();

        Integer totalStock = record.get("total_stock", Integer.class);
        return totalStock != null ? totalStock : 0;
    }

    /**
     * 查询库存低于阈值的所有商品
     *
     * @param threshold 库存阈值
     * @return 商品列表
     */
    public List<JooqProductDto> findLowStockProducts(int threshold) {
        Result<Record7<Long, String, String, BigDecimal, Integer, String, LocalDateTime>> records =
            dsl.select(ID, NAME, DESCRIPTION, PRICE, STOCK, CATEGORY, CREATED_AT)
                .from(table(TABLE_NAME))
                .where(STOCK.lessThan(threshold))
                .orderBy(STOCK.asc())
                .fetch();

        return records.map(this::mapToProductDto);
    }

    /**
     * 将 Record 映射为 ProductDto
     */
    private JooqProductDto mapToProductDto(Record record) {
        return JooqProductDto.builder()
            .id(record.get(ID))
            .name(record.get(NAME))
            .description(record.get(DESCRIPTION))
            .price(record.get(PRICE))
            .stock(record.get(STOCK))
            .category(record.get(CATEGORY))
            .createdAt(record.get(CREATED_AT))
            .updatedAt(record.get(UPDATED_AT))
            .build();
    }
}
