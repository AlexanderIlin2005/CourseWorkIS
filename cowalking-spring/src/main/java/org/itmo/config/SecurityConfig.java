// src/main/java/org/itmo/config/SecurityConfig.java
package org.itmo.config;

import org.itmo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.DelegatingFilterProxy; // <-- Добавьте импорт
import jakarta.servlet.Filter; // <-- Добавьте импорт

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    // Убедитесь, что имя метода @Bean -- 'filterChain'
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/registration", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form // Используем formLogin
                        .loginPage("/login") // Указываем страницу логина
                        .permitAll() // Разрешаем всем доступ к странице логина
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    // --- Добавленный бин для фильтра ---
    @Bean
    public Filter springSecurityFilterChainProxy() { // Имя бина может быть любым
        // Создаем DelegatingFilterProxy, указывая имя бина SecurityFilterChain
        // В данном случае, имя метода @Bean -- 'filterChain', значит, бин SecurityFilterChain будет называться 'filterChain'
        return new DelegatingFilterProxy("filterChain");
    }
    // --- Конец добавления бина ---

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder);
    }
}