package com.example.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.AddressDto;
import com.example.demo.entity.Address;

/**
 * 地址映射器
 *
 * @author chinwe
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    /**
     * Address -> AddressDto
     * 使用表达式组合完整地址
     */
    @Mapping(target = "fullAddress",
             expression = "java(address.getStreet() + \", \" + address.getCity() + \", \" + address.getState())")
    @Mapping(target = "postalCode", source = "zipCode")
    AddressDto toAddressDto(Address address);

    /**
     * AddressDto -> Address
     */
    @Mapping(target = "zipCode", source = "postalCode")
    Address toAddress(AddressDto dto);
}
