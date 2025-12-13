// src/main/java/org/itmo/controller/UserController.java
package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import org.itmo.service.EventService; // <-- Импортируем EventService
import org.itmo.mapper.EventMapper; // <-- Импортируем EventMapper
import org.itmo.mapper.UserMapper; // <-- Импортируем UserMapper
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // <-- Импортируем Logger

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // <-- Импортируем Authentication Token
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List; // <-- Импортируем List
import java.util.stream.Collectors; // <-- Импортируем Collectors

import org.itmo.dto.EventDto;
import org.itmo.dto.ParticipationDto;
import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus;

import org.itmo.model.Participation;

import org.itmo.mapper.ParticipationMapper;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper; // <-- Внедряем UserMapper

    // --- ВНЕДРИМ EventService и EventMapper ---
    @Autowired
    private EventService eventService; // <-- Внедряем EventService

    @Autowired
    private EventMapper eventMapper; // <-- Внедряем EventMapper

    // --- ВНЕДРИМ ParticipationMapper ---
    @Autowired
    private ParticipationMapper participationMapper; // <-- 2. ОБЪЯВИТЕ ПОЛЕ
    // --- КОНЕЦ ВНЕДРЕНИЯ ---


    // --- КОНЕЦ ВНЕДРЕНИЯ ---

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // <-- Логгер

    @GetMapping("/users/profile")
    public String profile(Model model) {
        logger.info("Handling GET request for /users/profile");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Current authentication: {}", auth);

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            logger.warn("User not authenticated or principal is not of type User, redirecting to /login");
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();
        logger.info("Current user: ID={}, Username={}", currentUser.getId(), currentUser.getUsername());

        // Convert User entity to UserDto for the view
        UserDto userDto = userMapper.toUserDto(currentUser);
        model.addAttribute("user", userDto);

        // Получаем все события пользователя
        List<Event> organizedEvents = userService.findOrganizedEvents(currentUser.getId());

        // Фильтруем активные и завершённые
        List<EventDto> activeOrganizedEvents = organizedEvents.stream()
                .filter(event -> {
                    // Обновляем статус при необходимости
                    eventService.updateEventStatusIfNeeded(event);
                    return EventStatus.ACTIVE.equals(event.getStatus());
                })
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());

        List<EventDto> completedOrganizedEvents = organizedEvents.stream()
                .filter(event -> {
                    // Обновляем статус при необходимости (на случай, если не обновился в активных)
                    eventService.updateEventStatusIfNeeded(event);
                    return EventStatus.COMPLETED.equals(event.getStatus());
                })
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());

        // --- ИСПРАВЛЕНО: Используем конструктор ParticipationDto ---
        // Заявки, отправленные пользователем
        List<Participation> sentApplications = userService.findPendingApplicationsSentByUser(currentUser.getId());
        List<ParticipationDto> sentApplicationDtos = sentApplications.stream()
                .map(ParticipationDto::new) // <-- Явный вызов конструктора
                .collect(Collectors.toList());
        model.addAttribute("sentApplications", sentApplicationDtos);

        // Заявки на события пользователя (если он организатор)
        List<Participation> receivedApplications = userService.findPendingApplicationsForOrganizer(currentUser.getId());
        List<ParticipationDto> receivedApplicationDtos = receivedApplications.stream()
                .map(ParticipationDto::new) // <-- Явный вызов конструктора
                .collect(Collectors.toList());
        model.addAttribute("receivedApplications", receivedApplicationDtos);
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        model.addAttribute("activeOrganizedEvents", activeOrganizedEvents);
        model.addAttribute("completedOrganizedEvents", completedOrganizedEvents);

        return "profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(@ModelAttribute UserDto userDto, Model model) {
        logger.info("Handling POST request for /users/update with DTO: {}", userDto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Current authentication: {}", auth);

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            logger.warn("User not authenticated or principal is not of type User, redirecting to /login");
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();
        logger.info("Current user attempting update: ID={}, Username={}", currentUser.getId(), currentUser.getUsername());

        // Check if updating own profile or admin updating
        if (!currentUser.getId().equals(userDto.getId()) && !currentUser.getRole().equals(UserRole.ADMIN)) {
            logger.warn("User {} (ID={}) attempted to update profile for user ID {}, access denied.", currentUser.getUsername(), currentUser.getId(), userDto.getId());
            return "redirect:/users/profile";
        }

        // Find the user to update (should be the same as current user or admin)
        User userToUpdate = userService.findById(userDto.getId())
                .orElseThrow(() -> {
                    logger.error("User to update (ID={}) not found in database.", userDto.getId());
                    return new RuntimeException("User not found");
                });
        logger.info("Found user in DB to update: ID={}, Username={}", userToUpdate.getId(), userToUpdate.getUsername());

        // Update allowed fields
        userToUpdate.setEmail(userDto.getEmail());
        userToUpdate.setPhone(userDto.getPhone());
        userToUpdate.setBio(userDto.getBio());
        // --- ДОБАВЛЕНО: ОБНОВЛЕНИЕ ID СОЦСЕТЕЙ ---
        userToUpdate.setTelegramId(userDto.getTelegramId());
        userToUpdate.setVkId(userDto.getVkId());
        // --- КОНЕЦ ДОБАВЛЕНИЯ ---

        logger.info("Updated fields for user {}: Email={}, Phone={}, Bio={}", userToUpdate.getId(), userToUpdate.getEmail(), userToUpdate.getPhone(), userToUpdate.getBio());

        try {
            User updatedUser = userService.save(userToUpdate);
            logger.info("User profile updated successfully: ID={}", updatedUser.getId());

            // --- ИСПРАВЛЕНО: Принудительно обновляем Authentication в SecurityContextHolder ---
            UserDetails freshUserDetails = userService.loadUserByUsername(updatedUser.getUsername());
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    freshUserDetails, // Новый Principal
                    auth.getCredentials(),
                    freshUserDetails.getAuthorities()
            );
            newAuth.setDetails(auth.getDetails());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

            model.addAttribute("message", "Profile updated successfully!");
            // Refresh the DTO to show updated values
            UserDto updatedUserDto = userMapper.toUserDto(updatedUser);
            model.addAttribute("user", updatedUserDto);

            // --- ПОВТОРНО ПОЛУЧАЕМ И ПЕРЕДАЕМ ОРГАНИЗОВАННЫЕ СОБЫТИЯ ---
            try {
                List<org.itmo.model.Event> organizedEvents = userService.findOrganizedEvents(updatedUser.getId());
                logger.debug("Found {} events organized by user ID={} after update", organizedEvents.size(), updatedUser.getId());

                List<org.itmo.dto.EventDto> organizedEventDtos = organizedEvents.stream()
                        .map(eventMapper::toEventDto)
                        .collect(Collectors.toList());

                model.addAttribute("organizedEvents", organizedEventDtos);
            } catch (Exception e) {
                logger.error("Error fetching events organized by user ID={} after update", updatedUser.getId(), e);
                model.addAttribute("organizedEvents", List.of()); // Передаем пустой список в случае ошибки
            }
            // --- КОНЕЦ ПОВТОРНОГО ПОЛУЧЕНИЯ ---
            return "redirect:/users/profile";
        } catch (Exception e) {
            logger.error("Error updating user profile (ID={}): {}", userDto.getId(), e.getMessage(), e);
            model.addAttribute("error", "Update failed: " + e.getMessage());
            model.addAttribute("user", userDto); // Pass DTO back to keep values

            // --- ПОЛУЧАЕМ И ПЕРЕДАЕМ ОРГАНИЗОВАННЫЕ СОБЫТИЯ ---
            try {
                User userForEvents = userService.findById(userDto.getId()).orElse(null);
                if (userForEvents != null) {
                    List<org.itmo.model.Event> organizedEvents = userService.findOrganizedEvents(userForEvents.getId());
                    logger.debug("Found {} events organized by user ID={} for error page", organizedEvents.size(), userForEvents.getId());

                    List<org.itmo.dto.EventDto> organizedEventDtos = organizedEvents.stream()
                            .map(eventMapper::toEventDto)
                            .collect(Collectors.toList());

                    model.addAttribute("organizedEvents", organizedEventDtos);
                } else {
                    model.addAttribute("organizedEvents", List.of());
                }
            } catch (Exception e2) {
                logger.error("Error fetching events organized by user ID={} for error page", userDto.getId(), e2);
                model.addAttribute("organizedEvents", List.of());
            }
            // --- КОНЕЦ ПОЛУЧЕНИЯ ---
            return "profile"; // Возвращаемся на ту же страницу с ошибкой
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

            // --- ДОБАВЛЕНО: ПРОВЕРКА УНИКАЛЬНОСТИ ID СОЦСЕТЕЙ ---
            // Check if Telegram ID exists (if provided)
            if (userDto.getTelegramId() != null && !userDto.getTelegramId().isEmpty() && userService.findByTelegramId(userDto.getTelegramId()).isPresent()) {
                model.addAttribute("error", "Telegram ID already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }

            // Check if VK ID exists (if provided)
            if (userDto.getVkId() != null && !userDto.getVkId().isEmpty() && userService.findByVkId(userDto.getVkId()).isPresent()) {
                model.addAttribute("error", "VK ID already exists.");
                model.addAttribute("userDto", userDto);
                return "registration";
            }
            // --- КОНЕЦ ДОБАВЛЕНИЯ ---

            // --- ИСПРАВЛЕНО: Не используем UserMapper для пароля ---
            User user = new User(); // Создаем сущность вручную
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setBio(userDto.getBio());
            // --- ДОБАВЛЕНО: УСТАНОВКА ID СОЦСЕТЕЙ ---
            user.setTelegramId(userDto.getTelegramId());
            user.setVkId(userDto.getVkId());
            // --- КОНЕЦ ДОБАВЛЕНИЯ ---
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