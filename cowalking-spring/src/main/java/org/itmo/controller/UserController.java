// src/main/java/org/itmo/controller/UserController.java
package org.itmo.controller;

import org.itmo.dto.UserDto; // Убедитесь, что импортируете DTO, а не сущность
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor // Lombok генерирует конструктор для final полей
public class UserController {

    private final UserService userService;

    @GetMapping("/users/profile")
    public String profile(Model model) {
        // ... существующая логика ...
        return "profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(@ModelAttribute User user, Model model) {
        // ... существующая логика ...
        return "profile";
    }

    // --- Добавлено: GET для страницы регистрации ---
    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("userDto", new UserDto()); // Теперь этот вызов должен работать
        return "registration"; // Возвращает имя шаблона
    }

    // --- Добавлено: POST для обработки регистрации ---
    @PostMapping("/registration")
    public String registerUser(@ModelAttribute UserDto userDto, Model model) {
        try {
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            // Используем getPassword() из DTO
            user.setPassword(userService.encodePassword(userDto.getPassword()));
            user.setRole(UserRole.PARTICIPANT); // Устанавливаем роль по умолчанию
            user.setActive(true);

            userService.save(user);
            model.addAttribute("message", "Registration successful!");
            return "login"; // Перенаправляем на страницу входа после успешной регистрации
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userDto", userDto); // Передаем DTO обратно, чтобы поля не очистились
            return "registration"; // Возвращаемся к форме регистрации с ошибкой
        }
    }
}