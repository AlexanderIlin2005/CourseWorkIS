package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.dto.EventDto;
import org.itmo.model.User;
import org.itmo.model.Event;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;

import org.itmo.service.EventService; // Импортируем EventService
import org.itmo.mapper.EventMapper; // Импортируем EventMapper

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Autowired
    private EventService eventService; // <-- Внедряем EventService

    @Autowired
    private EventMapper eventMapper; // <-- Внедряем EventMapper

    @GetMapping("/users/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        // Convert User entity to UserDto for the view
        UserDto userDto = new UserDto(currentUser);
        model.addAttribute("user", userDto);

        // --- ПОЛУЧАЕМ И ПЕРЕДАЕМ ОРГАНИЗОВАННЫЕ СОБЫТИЯ ---
        // Используем userService для получения событий, организованных пользователем
        List<Event> organizedEvents = userService.findOrganizedEvents(currentUser.getId());
        // Преобразуем их в DTO
        List<EventDto> organizedEventDtos = organizedEvents.stream()
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());
        model.addAttribute("organizedEvents", organizedEventDtos);
        // --- КОНЕЦ ПОЛУЧЕНИЯ ---

        return "profile";
    }

    // src/main/java/org/itmo/controller/UserController.java
// ...
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
            User updatedUser = userService.save(userToUpdate);
            model.addAttribute("message", "Profile updated successfully!");
            // Refresh the DTO to show updated values
            UserDto updatedUserDto = new UserDto(updatedUser);
            model.addAttribute("user", updatedUserDto);

            // --- ПОЛУЧАЕМ И ПЕРЕДАЕМ ОРГАНИЗОВАННЫЕ СОБЫТИЯ ---
            List<Event> organizedEvents = userService.findOrganizedEvents(currentUser.getId());
            List<EventDto> organizedEventDtos = organizedEvents.stream()
                    .map(eventMapper::toEventDto)
                    .collect(Collectors.toList());
            model.addAttribute("organizedEvents", organizedEventDtos);
            // --- КОНЕЦ ПОЛУЧЕНИЯ ---
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            model.addAttribute("user", userDto); // Pass DTO back to keep values
            // --- ПОЛУЧАЕМ И ПЕРЕДАЕМ ОРГАНИЗОВАННЫЕ СОБЫТИЯ ---
            List<Event> organizedEvents = userService.findOrganizedEvents(currentUser.getId());
            List<EventDto> organizedEventDtos = organizedEvents.stream()
                    .map(eventMapper::toEventDto)
                    .collect(Collectors.toList());
            model.addAttribute("organizedEvents", organizedEventDtos);
            // --- КОНЕЦ ПОЛУЧЕНИЯ ---
        }

        return "profile";
    }
// ...

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

            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone()); // Added phone
            user.setBio(userDto.getBio());     // Added bio
            // Password will be encoded in UserService.save()
            user.setPassword(userDto.getPassword());
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
