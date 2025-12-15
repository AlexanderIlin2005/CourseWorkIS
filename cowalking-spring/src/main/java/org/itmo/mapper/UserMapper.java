
package org.itmo.mapper;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true) 
    @Mapping(target = "active", ignore = true)   
    @Mapping(target = "createdAt", ignore = true) 
    @Mapping(target = "updatedAt", ignore = true) 
    @Mapping(target = "role", ignore = true) 
    User toUser(UserDto userDto);
}