package org.itmo.config;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements InitializingBean {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        // Initialize default admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(userService.encodePassword("admin"));
            admin.setRole(UserRole.ADMIN);
            admin.setEmail("admin@cowalking.com");
            admin.setActive(true);
            userService.save(admin);
        }
    }
}
