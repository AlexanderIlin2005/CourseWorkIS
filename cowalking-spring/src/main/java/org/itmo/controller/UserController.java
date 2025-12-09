package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
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

    @GetMapping("/users/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
             return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        // Преобразуем User в UserDto для представления
        UserDto userDto = new UserDto(currentUser);
        model.addAttribute("user", userDto);
        return "profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(@ModelAttribute UserDto userDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Проверяем, что пользователь обновляет только себя
        if (!currentUser.getId().equals(userDto.getId()) &&
            !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/users/profile";
        }

        // Обновляем только разрешенные поля (email, username)
        currentUser.setEmail(userDto.getEmail());
        // username обычно не меняется после регистрации

        try {
            User updatedUser = userService.save(currentUser);
            model.addAttribute("message", "Profile updated successfully!");
            // Преобразуем обновленного User в UserDto для представления
            UserDto updatedUserDto = new UserDto(updatedUser);
            model.addAttribute("user", updatedUserDto);
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            model.addAttribute("user", userDto);
        }

        return "profile";
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute UserDto userDto, Model model) {
        try {
            // Проверяем, что username и email уникальны
            if (userService.findByUsername(userDto.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }
            if (userService.findByEmail(userDto.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            // Пароль будет захеширован в UserService.save()
            user.setPassword(userDto.getPassword());
            user.setRole(UserRole.PARTICIPANT); // Роль по умолчанию
            user.setActive(true);

            User savedUser = userService.save(user); // Вызов save() вызовет passwordEncoder.encode()
            model.addAttribute("message", "Registration successful! Please log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userDto", userDto);
            return "registration";
        }
    }
}
