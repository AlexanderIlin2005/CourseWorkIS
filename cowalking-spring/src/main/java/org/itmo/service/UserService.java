package org.itmo.service;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {
        // Encode password if it's not already encoded (e.g., during registration)
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) { // Assuming BCrypt starts with $2a$
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // --- ADDED: Method to find all users ---
    public List<User> findAll() {
        return userRepository.findAll();
    }
    // --- END OF ADDITION ---

    // --- ADDED: Method to find by email ---
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    // --- END OF ADDITION ---

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user;
    }
}
