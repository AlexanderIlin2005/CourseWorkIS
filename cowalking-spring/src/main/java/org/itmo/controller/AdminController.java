
package org.itmo.controller;

import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/freeze")
    public String freezeUser(@PathVariable Long userId) {
        userService.freezeUser(userId);
        return "redirect:/admin/users";
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/unfreeze")
    public String unfreezeUser(@PathVariable Long userId) {
        userService.unfreezeUser(userId);
        return "redirect:/admin/users";
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/role")
    public String changeUserRole(@PathVariable Long userId, @RequestParam UserRole newRole) {
        userService.changeUserRole(userId, newRole);
        return "redirect:/admin/users";
    }
}