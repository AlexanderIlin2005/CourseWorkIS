
package org.itmo.config;

import org.itmo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService; 

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class); 

    @Bean
    public PasswordEncoder passwordEncoder() { 
        logger.info("Creating PasswordEncoder bean: BCryptPasswordEncoder"); 
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain"); 
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/registration", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                        .failureUrl("/login?error") 
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    
    
    
    
    
    
    
    
    
}