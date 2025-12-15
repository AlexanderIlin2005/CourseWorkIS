
package org.itmo.controller;

import org.itmo.dto.ReviewDto;
import org.itmo.model.User;
import org.itmo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/events/{eventId}/review")
    public String submitReview(@PathVariable Long eventId,
                               @ModelAttribute ReviewDto reviewDto,
                               RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        try {
            reviewService.createOrUpdateReview(eventId, reviewDto, currentUser);
            redirectAttributes.addFlashAttribute("message", "Your review has been submitted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit review: " + e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }
}