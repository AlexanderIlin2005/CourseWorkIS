// src/main/java/org/itmo/controller/HomeController.java
package org.itmo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Маппинг на "/" теперь ведет на index.html
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Маппинг на "/index" также ведет на index.html
    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}