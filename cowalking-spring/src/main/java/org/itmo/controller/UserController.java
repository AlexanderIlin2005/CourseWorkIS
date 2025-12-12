// src/main/java/org/itmo/controller/UserController.java
package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import org.itmo.mapper.UserMapper; // <-- Импортируем UserMapper
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper; // <-- Внедряем UserMapper

    @GetMapping("/users/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        // Convert User entity to UserDto for the view
        UserDto userDto = userMapper.toUserDto(currentUser); // <-- Используем UserMapper
        model.addAttribute("user", userDto);

        return "profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(@ModelAttribute UserDto userDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        // Check if updating own profile or admin updating
        if (!currentUser.getId().equals(userDto.getId()) && !currentUser.getRole().equals(UserRole.ADMIN)) {
            return "redirect:/users/profile";
        }

        // Find the user to update (should be the same as current user or admin)
        User userToUpdate = userService.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields
        userToUpdate.setEmail(userDto.getEmail());
        userToUpdate.setPhone(userDto.getPhone()); // <-- Обновляем телефон
        userToUpdate.setBio(userDto.getBio());     // <-- Обновляем bio

        try {
            User updatedUser = userService.save(userToUpdate); // <-- Сохраняем обновленную сущность
            model.addAttribute("message", "Profile updated successfully!");
            // Refresh the DTO to show updated values
            UserDto updatedUserDto = userMapper.toUserDto(updatedUser); // <-- Используем UserMapper
            model.addAttribute("user", updatedUserDto);
            return "redirect:/users/profile"; // <-- REDIRECT после POST!
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            // Pass DTO back to keep values
            model.addAttribute("user", userDto);
            return "profile"; // <-- Возвращаемся на ту же страницу с ошибкой
        }
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "registration";
    }


    @PostMapping("/registration")
    public String registerUser(@ModelAttribute UserDto userDto, Model model) {
        try {
            // Validate phone format if provided (basic check for Russian format)
            if (userDto.getPhone() != null && !userDto.getPhone().isEmpty() && !userDto.getPhone().matches("^\\+7\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$")) {
                model.addAttribute("error", "Invalid phone number format. Use +7(xxx)-xxx-xx-xx.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            // Check if username exists
            if (userService.findByUsername(userDto.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            // Check if email exists
            if (userService.findByEmail(userDto.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            // Check if phone exists (if phone is provided)
            if (userDto.getPhone() != null && !userDto.getPhone().isEmpty() && userService.findByPhone(userDto.getPhone()).isPresent()) {
                model.addAttribute("error", "Phone number already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            // --- ИСПРАВЛЕНО: Не используем UserMapper для пароля ---
            User user = new User(); // Создаем сущность вручную
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setBio(userDto.getBio());
            // Хешируем пароль из DTO и устанавливаем в сущность
            user.setPassword(userService.encodePassword(userDto.getPassword()));
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
            user.setRole(UserRole.PARTICIPANT); // Default role
            user.setActive(true);

            User savedUser = userService.save(user);
            model.addAttribute("message", "Registration successful! Please log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userDto", userDto);
            return "registration";
        }
    }
}