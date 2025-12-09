package org.itmo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // --- УБРАНО: @GetMapping("/registration") ---
    // Логика регистрации теперь в UserController
    // @GetMapping("/registration")
    // public String registration() {
    //     return "registration";
    // }
    // --- КОНЕЦ УБРАНОГО ---
}
