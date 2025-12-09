package org.itmo.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

// Наследуемся от AbstractSecurityWebApplicationInitializer
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
    // Конструктор по умолчанию обычно достаточен
    // Он автоматически зарегистрирует DelegatingFilterProxy для 'springSecurityFilterChain'
    public SecurityInitializer() {
        // super(); // Вызов конструктора родителя, который и делает всю работу
        // super(SecurityConfig.class); // Можно указать SecurityConfig.class, если фильтр должен быть инициализирован до корневого контекста,
        // но обычно этого не требуется, если SecurityConfig в корневом контексте.
    }
}
