
package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); 

    @GetMapping("/users/profile")
    public String profile(Model model) {
        
        return "profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(@ModelAttribute User user, Model model) {
        
        return "profile";
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        logger.info("Displaying registration form"); 
        model.addAttribute("userDto", new UserDto());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute UserDto userDto, Model model) {
        logger.info("Processing registration request for username: {}", userDto.getUsername()); 
        try {
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            String rawPassword = userDto.getPassword();
            logger.debug("Received raw password for registration: {}", rawPassword); 
            
            String encodedPassword = userService.encodePassword(rawPassword); 
            user.setPassword(encodedPassword);
            user.setRole(UserRole.PARTICIPANT);
            user.setActive(true);

            User savedUser = userService.save(user); 
            logger.info("User registered successfully with ID: {}", savedUser.getId()); 
            model.addAttribute("message", "Registration successful!");
            return "login";
        } catch (Exception e) {
            logger.error("Registration failed for username '{}': {}", userDto.getUsername(), e.getMessage(), e); 
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userDto", userDto);
            return "registration";
        }
    }
}