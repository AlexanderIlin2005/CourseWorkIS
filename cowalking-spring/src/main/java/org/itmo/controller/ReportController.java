package org.itmo.controller;

import org.itmo.dto.ReportDto;
import org.itmo.model.Event;
import org.itmo.model.Report;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.service.EventService;
import org.itmo.service.ReportService;
import org.itmo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @GetMapping("/create")
    public String createReportForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
             return "redirect:/login";
        }

        User currentUser = (User) auth.getPrincipal();
        
        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);

        model.addAttribute("report", new ReportDto());
        
        if (isAdmin) {
            model.addAttribute("users", userService.findAll()); 
            model.addAttribute("events", eventService.findAll());
        }
        return "reports/create";
    }

    @PostMapping("/create")
    public String createReport(@ModelAttribute ReportDto reportDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            Report report = new Report();
            report.setReason(reportDto.getReason());
            report.setReporter(currentUser);

            
            if (reportDto.getReportedUserId() != null) {
                if (!currentUser.getRole().equals(UserRole.ADMIN)) {
                    model.addAttribute("error", "Only admins can report specific users.");
                    model.addAttribute("report", reportDto);
                    return "reports/create";
                }
                User reportedUser = userService.findById(reportDto.getReportedUserId())
                    .orElseThrow(() -> new RuntimeException("Reported user not found"));
                report.setReportedUser(reportedUser);
            }

            
            if (reportDto.getEventId() != null) {
                Event event = eventService.findById(reportDto.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
                report.setEvent(event);
            }

            Report savedReport = reportService.createReport(report, currentUser);
            model.addAttribute("message", "Report submitted successfully!");
            return "redirect:/reports/create";
        } catch (Exception e) {
            model.addAttribute("error", "Report creation failed: " + e.getMessage());
            model.addAttribute("report", reportDto);
            
            if (currentUser.getRole().equals(UserRole.ADMIN)) {
                model.addAttribute("users", userService.findAll());
                model.addAttribute("events", eventService.findAll());
            }
            return "reports/create";
        }
    }

    @GetMapping
    public String listReports(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
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

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/reports";
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
