
package org.itmo.dto;

import org.itmo.model.enums.UserRole;
import lombok.Data; 
import lombok.NoArgsConstructor; 

@Data
@NoArgsConstructor 
public class UserDto {
    private Long id; 
    private String username;
    private String email;
    private String password; 
    private UserRole role;   

    
    
    
    
    
    
}
