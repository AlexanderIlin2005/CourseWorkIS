package org.itmo.config;

import org.itmo.service.UserService; // Ensure imported
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService; // Service for UserDetailsService

    @Autowired
    private PasswordEncoder passwordEncoder; // Service for PasswordEncoder

    @Bean
    public UserDetailsService userDetailsService() {
        return userService; // Return your UserService (UserDetailsService)
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/registration", "/css/**", "/js/**", "/images/**", "/login").permitAll() // Allow access
                        .requestMatchers("/events", "/events/{id}").permitAll() // Allow viewing events
                        .requestMatchers("/events/create", "/events/{id}/edit", "/events/{id}/delete").authenticated() // Require authentication for CRUD ops
                        .requestMatchers("/participations/join/**", "/participations/leave/**").authenticated() // Require authentication for participation
                        .requestMatchers("/reports/create").authenticated() // Require authentication for reporting
                        .requestMatchers("/reports").hasRole("ADMIN") // Only admin can view reports
                        .requestMatchers("/reports/{id}/resolve").hasRole("ADMIN") // Only admin can resolve
                        .anyRequest().authenticated() // All other require authentication
                )
                .formLogin((form) -> form
                        .loginPage("/login") // Specify login page
                        .permitAll() // Allow everyone access to login page
                )
                .logout(LogoutConfigurer::permitAll); // Allow everyone to logout

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder);
    }
}
