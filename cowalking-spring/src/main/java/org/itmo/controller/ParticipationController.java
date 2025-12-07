// src/main/java/org/itmo/controller/ParticipationController.java
package org.itmo.controller;

import org.itmo.model.User;
import org.itmo.service.ParticipationService;
import org.itmo.service.UserService; // <-- Импортируем UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ParticipationController {

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private UserService userService; // <-- Внедряем UserService

    @PostMapping("/participations/join/{eventId}")
    public String joinEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        // --- ИМИТАЦИЯ: Всегда используем пользователя admin ---
        User adminUser = userService.findByUsername("admin") // Находим администратора
                .orElseThrow(() -> new RuntimeException("Admin user not found for demo purposes."));
        // --- КОНЕЦ ИМИТАЦИИ ---

        try {
            participationService.joinEvent(eventId, adminUser); // <-- Используем adminUser
            redirectAttributes.addFlashAttribute("message", "Successfully joined the event!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }

    @PostMapping("/participations/leave/{eventId}")
    public String leaveEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        // --- ИМИТАЦИЯ: Всегда используем пользователя admin ---
        User adminUser = userService.findByUsername("admin") // Находим администратора
                .orElseThrow(() -> new RuntimeException("Admin user not found for demo purposes."));
        // --- КОНЕЦ ИМИТАЦИИ ---

        try {
            participationService.leaveEvent(eventId, adminUser); // <-- Используем adminUser
            redirectAttributes.addFlashAttribute("message", "Successfully left the event!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }
}