package com.example.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 将 User 对象映射为 UserDTO
    UserDto toUserDto(User user);

    // 将 UserDTO 映射为 User 对象
    @Mapping(target = "id", ignore = true)   
    User toUser(UserDto userDto);
}
