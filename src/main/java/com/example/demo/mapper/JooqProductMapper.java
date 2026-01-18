package com.example.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqProductDto;

/**
 * JOOQ 商品映射器
 *
 * @author chinwe
 */
@Mapper(componentModel = "spring")
public interface JooqProductMapper {

    /**
     * 将创建商品请求映射为商品 DTO
     * 忽略 id, createdAt, updatedAt 字段
     *
     * @param request 创建商品请求
     * @return 商品 DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JooqProductDto toProductDto(JooqCreateProductRequest request);
}
