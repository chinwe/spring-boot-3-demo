package com.example.demo.repository.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record6;
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

    // 字段定义
    private static final Field<Long> ID = field("id", BIGINT);
    private static final Field<String> USERNAME = field("username", VARCHAR);
    private static final Field<String> EMAIL = field("email", VARCHAR);
    private static final Field<String> PHONE = field("phone", VARCHAR);
    private static final Field<LocalDateTime> CREATED_AT = field("created_at", LOCALDATETIME);
    private static final Field<LocalDateTime> UPDATED_AT = field("updated_at", LOCALDATETIME);

    public JooqUserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 插入用户
     *
     * @param user 用户 DTO
     * @return 生成的 ID
     * @throws IllegalArgumentException 如果 user 为 null
     */
    public Long insert(JooqUserDto user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Record record = dsl.insertInto(
                table(TABLE_NAME),
                USERNAME, EMAIL, PHONE, CREATED_AT, UPDATED_AT
            )
            .values(
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                LocalDateTime.now(),
                LocalDateTime.now()
            )
            .returning(ID)
            .fetchOne();

        return record.get(ID);
    }

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户 DTO，不存在返回 null
     */
    public JooqUserDto findById(Long id) {
        Record record = dsl.select(ID, USERNAME, EMAIL, PHONE, CREATED_AT, UPDATED_AT)
            .from(table(TABLE_NAME))
            .where(ID.eq(id))
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
        Record record = dsl.select(ID, USERNAME, EMAIL, PHONE, CREATED_AT, UPDATED_AT)
            .from(table(TABLE_NAME))
            .where(USERNAME.eq(username))
            .fetchOne();

        return record != null ? mapToUserDto(record) : null;
    }

    /**
     * 更新用户
     *
     * @param user 用户 DTO
     * @return 是否更新成功
     * @throws IllegalArgumentException 如果 user 为 null 或 user ID 为 null
     */
    public boolean update(JooqUserDto user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        int affected = dsl.update(table(TABLE_NAME))
            .set(USERNAME, user.getUsername())
            .set(EMAIL, user.getEmail())
            .set(PHONE, user.getPhone())
            .set(UPDATED_AT, LocalDateTime.now())
            .where(ID.eq(user.getId()))
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
            .where(ID.eq(id))
            .execute();

        return affected > 0;
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    public List<JooqUserDto> findAll() {
        Result<Record6<Long, String, String, String, LocalDateTime, LocalDateTime>> records =
            dsl.select(ID, USERNAME, EMAIL, PHONE, CREATED_AT, UPDATED_AT)
                .from(table(TABLE_NAME))
                .fetch();

        return records.map(this::mapToUserDto);
    }

    /**
     * 将 Record 映射为 UserDto
     */
    private JooqUserDto mapToUserDto(Record record) {
        return JooqUserDto.builder()
            .id(record.get(ID))
            .username(record.get(USERNAME))
            .email(record.get(EMAIL))
            .phone(record.get(PHONE))
            .createdAt(record.get(CREATED_AT))
            .updatedAt(record.get(UPDATED_AT))
            .build();
    }
}
