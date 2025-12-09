package org.itmo.controller;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus; // Import the external enum
import org.itmo.service.EventService;
import org.itmo.service.LocationService;
import org.itmo.service.UserService;
import org.itmo.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final LocationService locationService;
    private final UserService userService; // <-- Inject UserService for fake user simulation
    private final EventMapper eventMapper; // <-- Inject EventMapper

    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventService.findAll();
        // Convert list of Event to list of EventDto
        List<EventDto> eventDtos = events.stream()
                .map(eventMapper::toEventDto) // Use mapper
                .collect(Collectors.toList());
        model.addAttribute("events", eventDtos); // Pass DTO to model
        return "events/list";
    }

    @GetMapping("/create")
    public String createEventForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
             return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        // Check permissions: only ADMIN or ORGANIZER can create
        if (!currentUser.getRole().equals(org.itmo.model.enums.UserRole.ORGANIZER) &&
            !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events";
        }

        model.addAttribute("event", new EventDto()); // Pass DTO to model
        model.addAttribute("locations", locationService.findAll());
        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute EventDto eventDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Check permissions: only ADMIN or ORGANIZER can create
        if (!currentUser.getRole().equals(org.itmo.model.enums.UserRole.ORGANIZER) &&
            !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events";
        }

        // Convert DTO to Event entity
        Event event = eventMapper.toEvent(eventDto);
        if (event.getId() == null) { // If creating new
            event.setStatus(EventStatus.ACTIVE); // Use external enum
            event.setOrganizer(currentUser); // Set current user as organizer
        }

        try {
            Event savedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + savedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Creation failed: " + e.getMessage());
            // On error, pass DTO back to model
            model.addAttribute("event", eventDto);
            model.addAttribute("locations", locationService.findAll());
            return "events/create";
        }
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        // Convert Event entity to DTO for presentation
        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto); // Pass DTO to model
        return "events/details";
    }

    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Check permissions: only organizer or admin can edit
        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        // Convert Event entity to DTO for form
        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto); // Pass DTO to model
        model.addAttribute("locations", locationService.findAll());
        return "events/edit";
    }

    @PostMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, @ModelAttribute EventDto eventDto, Model model) {
        Event existingEvent = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Check permissions: only organizer or admin can edit
        if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        // Convert DTO to Event entity
        Event event = eventMapper.toEvent(eventDto);
        // Preserve ID and organizer from existing event
        event.setId(id);
        event.setOrganizer(existingEvent.getOrganizer());

        try {
            Event updatedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + updatedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            // On error, pass DTO back to model
            model.addAttribute("event", eventDto);
            model.addAttribute("locations", locationService.findAll());
            return "events/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        eventService.deleteById(id, currentUser);
        return "redirect:/events";
    }
}
