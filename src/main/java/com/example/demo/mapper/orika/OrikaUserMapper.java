package com.example.demo.mapper.orika;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;

/**
 * Orika User 映射器 - MapStruct 实现
 *
 * 功能特性：
 * 1. User -> UserDto 映射
 * 2. UserDto -> User 反向映射
 * 3. 同名字段自动映射
 * 4. 类型安全保证
 *
 * @author chinwe
 */
@Mapper(componentModel = "spring")
public interface OrikaUserMapper {

    /**
     * User -> UserDto 映射
     *
     * @param user 源 User 对象
     * @return 映射后的 UserDto 对象
     */
    UserDto toUserDto(User user);

    /**
     * UserDto -> User 反向映射
     *
     * @param dto 源 UserDto 对象
     * @return 映射后的 User 对象
     */
    @Mapping(target = "id", ignore = true)
    User toUser(UserDto dto);
}
