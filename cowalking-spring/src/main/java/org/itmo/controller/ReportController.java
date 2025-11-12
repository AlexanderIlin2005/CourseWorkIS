package org.itmo.controller;

import org.itmo.model.Report;
import org.itmo.model.User;
import org.itmo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/create")
    public String createReportForm(Model model) {
        model.addAttribute("report", new Report());
        return "reports/create";
    }

    @PostMapping("/create")
    public String createReport(@ModelAttribute Report report, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            reportService.createReport(report, currentUser);
            model.addAttribute("message", "Report submitted successfully!");
            return "redirect:/reports/create";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("report", report);
            return "reports/create";
        }
    }

    @GetMapping
    public String listReports(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/";
        }

        model.addAttribute("reports", reportService.findAll());
        return "reports/list";
    }

    @PostMapping("/{id}/resolve")
    public String resolveReport(@PathVariable Long id, @RequestParam String resolutionNotes, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            reportService.resolveReport(id, resolutionNotes, currentUser);
            model.addAttribute("message", "Report resolved successfully!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/reports";
    }
}
