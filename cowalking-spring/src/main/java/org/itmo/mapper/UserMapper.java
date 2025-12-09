// src/main/java/org/itmo/mapper/UserMapper.java
package org.itmo.mapper;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // УБРАНО: @Mapping(target = "authorities", ignore = true) // <-- authorities нет в UserDto, нечего игнорировать при User -> UserDto
    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true) // Игнорируем пароль при создании сущности из DTO (он устанавливается в UserService)
    @Mapping(target = "active", ignore = true)   // Игнорируем active при создании сущности из DTO (устанавливается в UserService)
    // УБРАНО: @Mapping(target = "authorities", ignore = true) // <-- authorities нет в UserDto, нечего игнорировать при UserDto -> User
    @Mapping(target = "createdAt", ignore = true) // Игнорируем createdAt при создании сущности из DTO (устанавливается в UserService)
    @Mapping(target = "updatedAt", ignore = true) // Игнорируем updatedAt при создании сущности из DTO (устанавливается в UserService)
    @Mapping(target = "role", ignore = true) // Игнорируем role при создании сущности из DTO (устанавливается в UserService)
    User toUser(UserDto userDto);
}