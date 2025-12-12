// src/main/java/org/itmo/controller/PublicProfileController.java
package org.itmo.controller;

import org.itmo.dto.UserDto;
import org.itmo.model.User;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PublicProfileController {

    private final UserService userService;

    @GetMapping("/users/profile/{userId}")
    public String showPublicProfile(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDto userDto = new UserDto(user);
        // Убираем чувствительные данные (если нужно, но email/phone/bio публичны по ТЗ)
        model.addAttribute("user", userDto);
        return "public-profile";
    }
}