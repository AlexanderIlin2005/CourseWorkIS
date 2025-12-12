// src/main/java/org/itmo/dto/UserDto.java
package org.itmo.dto;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone; // Added phone field
    private String bio;   // Added bio field
    private String password; // Field for password during registration
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone(); // Added phone mapping
        this.bio = user.getBio();     // Added bio mapping

        this.role = user.getRole();
        this.active = user.isEnabled(); // Use isEnabled() instead of getActive()
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}