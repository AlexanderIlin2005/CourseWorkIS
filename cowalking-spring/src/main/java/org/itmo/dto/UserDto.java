
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
    private String phone; 
    private String bio;   
    private String password; 

    
    private String telegramId; 
    private String vkId;       
    

    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    private String photoUrl;
    

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone(); 
        this.bio = user.getBio();     

        
        this.telegramId = user.getTelegramId();
        this.vkId = user.getVkId();
        

        this.role = user.getRole();
        this.active = user.isEnabled(); 
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();

        this.photoUrl = user.getPhotoUrl();
    }
}