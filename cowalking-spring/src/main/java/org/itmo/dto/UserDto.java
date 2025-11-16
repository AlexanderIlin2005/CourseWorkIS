// src/main/java/org/itmo/dto/UserDto.java
package org.itmo.dto;

import org.itmo.model.enums.UserRole;
import lombok.Data; // @Data включает @Getter, @Setter, @ToString, @EqualsAndHashCode и @RequiredArgsConstructor (для final полей)
import lombok.NoArgsConstructor; // Добавляем конструктор по умолчанию

@Data
@NoArgsConstructor // Добавляем конструктор по умолчанию
public class UserDto {
    private Long id; // Может понадобиться, но необязательно для формы регистрации
    private String username;
    private String email;
    private String password; // Добавляем поле password
    private UserRole role;   // Может понадобиться, но необязательно для формы регистрации

    // Конструктор с параметрами (опционально, можно сгенерировать Lombok @AllArgsConstructor)
    // public UserDto(String username, String email, String password) {
    //     this.username = username;
    //     this.email = email;
    //     this.password = password;
    // }
}
