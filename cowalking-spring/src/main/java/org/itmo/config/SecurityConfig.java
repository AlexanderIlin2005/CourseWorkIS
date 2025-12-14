// src/main/java/org/itmo/config/SecurityConfig.java
package org.itmo.config;

import org.itmo.service.UserService; // Убедитесь, что импортирован
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

import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService; // Сервис для UserDetailsService

    @Autowired
    private PasswordEncoder passwordEncoder; // Сервис для PasswordEncoder



    @Bean
    public UserDetailsService userDetailsService() {
        return userService; // Возвращаем ваш UserService (UserDetailsService)
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Правильный подход - используем XorCsrfTokenRequestAttributeHandler
        CsrfTokenRequestAttributeHandler requestHandler = new XorCsrfTokenRequestAttributeHandler();

        http
                .csrf((csrf) -> csrf
                        .csrfTokenRequestHandler(requestHandler) // Используем обработчик
                        .ignoringRequestMatchers("/login") // Игнорируем логин (опционально)
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/registration", "/css/**", "/js/**", "/images/**", "/login").permitAll() // Разрешаем доступ
                        .requestMatchers("/events", "/events/{id}").permitAll() // Разрешаем просматривать события всем
                        .requestMatchers("/events/completed", "/events/active").permitAll() // Разрешаем просмотр завершённых всем
                        // --- ИСПРАВЛЕНО: разрешаем аутентифицированным пользователям создавать события ---
                        .requestMatchers("/events/create").authenticated() // <-- Только аутентифицированные могут создавать
                        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
                        .requestMatchers("/participations/join/**", "/participations/leave/**").authenticated() // Требует аутентификации
                        .requestMatchers("/reports/create").authenticated() // Требует аутентификации
                        .requestMatchers("/reports", "/reports/{id}/resolve").hasRole("ADMIN") // Только админ
                        // --- ИСПРАВЛЕНО: разрешаем аутентифицированным пользователям редактировать/удалять (проверка прав внутри контроллера) ---
                        .requestMatchers("/events/{id}/edit", "/events/{id}/delete").authenticated() // <-- Только аутентифицированные могут редактировать/удалять
                        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
                        // --- ДОБАВЛЕНО: разрешаем аутентифицированным пользователям обновлять профиль ---
                        .requestMatchers("/users/update").authenticated() // <-- Только аутентифицированные могут обновлять профиль
                        // --- КОНЕЦ ДОБАВЛЕНИЯ ---
                        // --- ДОБАВЛЕНО: Разрешить доступ к публичному профилю ---
                        .requestMatchers("/users/profile/{id}").permitAll()
                        // --- КОНЕЦ ДОБАВЛЕНИЯ ---
                        .requestMatchers("/admin/**").hasRole("ADMIN") // <-- Только админ может управлять пользователями
                        .anyRequest().authenticated() // Все остальные требуют аутентификации
                )
                .formLogin((form) -> form
                        .loginPage("/login") // Указываем страницу логина
                        .permitAll() // Разрешаем всем доступ к странице логина
                )
                .logout(LogoutConfigurer::permitAll); // Разрешить всем выход

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder);
    }
}
