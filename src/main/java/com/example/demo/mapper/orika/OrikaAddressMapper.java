package com.example.demo.mapper.orika;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.AddressDto;
import com.example.demo.entity.Address;

/**
 * Orika Address 映射器 - MapStruct 实现
 *
 * 功能特性：
 * 1. Address -> AddressDto 映射
 * 2. 用于 Orika 模块的嵌套对象映射演示
 *
 * @author chinwe
 */
@Mapper(componentModel = "spring")
public interface OrikaAddressMapper {

    /**
     * Address -> AddressDto 映射
     *
     * 映射规则：
     * - street -> fullAddress (简化处理)
     * - city -> city
     * - country -> country
     * - zipCode -> postalCode
     */
    @Mapping(target = "fullAddress", source = "street")
    @Mapping(target = "postalCode", source = "zipCode")
    AddressDto toAddressDto(Address address);
}
