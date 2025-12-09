package org.itmo.dto;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password; // Field for password during registration
    private UserRole role;
    private Boolean active; // This field might not be needed if not passed from User entity

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        // Don't set password for DTO
        this.role = user.getRole();
        // this.active = user.getActive(); // <-- REMOVED: User entity doesn't have getActive() method, it has isEnabled(), isAccountNonLocked(), etc.
        // If you need 'active' status in DTO, you can derive it from isEnabled():
        // this.active = user.isEnabled(); // Example, adjust based on your needs
    }
}
