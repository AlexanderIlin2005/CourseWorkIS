
package org.itmo.service;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class); 

    public User save(User user) {
        logger.info("Saving user: {}", user.getUsername()); 
        String rawPassword = user.getPassword();
        logger.debug("Raw password before encoding: {}", rawPassword); 
        String encodedPassword = passwordEncoder.encode(rawPassword);
        logger.debug("Encoded password: {}", encodedPassword); 
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        logger.info("Saved user with ID: {}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> findById(Long id) {
        logger.debug("Finding user by ID: {}", id); 
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username); 
        return userRepository.findByUsername(username);
    }

    public String encodePassword(String rawPassword) {
        logger.debug("Encoding password: {}", rawPassword); 
        String encoded = passwordEncoder.encode(rawPassword);
        logger.debug("Encoded password result: {}", encoded); 
        return encoded;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username: {}", username); 
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username); 
                    return new UsernameNotFoundException("User not found: " + username);
                });
        logger.info("Found user: {} with role: {}", user.getUsername(), user.getRole()); 
        logger.debug("User password hash: {}", user.getPassword()); 
        return user;
    }
}