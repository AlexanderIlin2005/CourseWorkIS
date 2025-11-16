// src/main/java/org/itmo/config/SecurityInitializer.java
package org.itmo.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

// Наследуемся от AbstractSecurityWebApplicationInitializer
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
    // Конструктор по умолчанию обычно достаточен
    // Он автоматически регистрирует DelegatingFilterProxy для 'springSecurityFilterChain'
    public SecurityInitializer() {
        // super(); // Явный вызов не обязателен
        // super(SecurityConfig.class); // Можно указать конфиг, если нужно инициализировать до корневого контекста
    }
}