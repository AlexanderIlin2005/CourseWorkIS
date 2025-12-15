package org.itmo.controller;

import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.service.EventService;
import org.itmo.service.ParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ParticipationController {

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private EventService eventService;

    @PostMapping("/participations/join/{eventId}")
    public String joinEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            participationService.joinEvent(eventId, currentUser);
            redirectAttributes.addFlashAttribute("message", "Successfully joined the event!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        
        return "redirect:/events/active";
        
    }

    @PostMapping("/participations/leave/{eventId}")
    public String leaveEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            participationService.leaveEvent(eventId, currentUser);
            redirectAttributes.addFlashAttribute("message", "Successfully left the event!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        
        return "redirect:/events/active";
        
    }

    
    @PostMapping("/participations/confirm/{participationId}")
    public String confirmParticipation(@PathVariable Long participationId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            participationService.confirmParticipation(participationId, currentUser);
            redirectAttributes.addFlashAttribute("message", "Application confirmed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        
        
        
        
        
        
        return "redirect:/users/profile";
    }

    
    @PostMapping("/participations/reject/{participationId}")
    public String rejectParticipation(@PathVariable Long participationId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            participationService.rejectParticipation(participationId, currentUser);
            redirectAttributes.addFlashAttribute("message", "Application rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/users/profile";
    }
}
