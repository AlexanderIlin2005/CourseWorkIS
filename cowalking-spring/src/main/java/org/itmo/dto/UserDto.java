package org.itmo.dto;

import org.itmo.model.enums.UserRole;
import lombok.Value;

@Value
public class UserDto {
    private Long id;
    String username;
    String email;
    UserRole role;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}
