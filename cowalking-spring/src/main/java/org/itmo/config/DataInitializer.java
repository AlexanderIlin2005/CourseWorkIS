
package org.itmo.config;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements InitializingBean {

    private final UserService userService;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class); 

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Running DataInitializer"); 
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Admin user not found. Creating default admin user."); 
            User admin = new User();
            admin.setUsername("admin");
            String rawPassword = "admin";
            logger.debug("Raw password for default admin: {}", rawPassword); 
            String encodedPassword = userService.encodePassword(rawPassword); 
            admin.setPassword(encodedPassword);
            admin.setRole(UserRole.ADMIN);
            admin.setEmail("admin@cowalking.com");
            admin.setActive(true);

            User savedAdmin = userService.save(admin); 
            logger.info("Default admin user created with ID: {}", savedAdmin.getId()); 
        } else {
            logger.info("Admin user already exists, skipping creation."); 
        }
    }
}