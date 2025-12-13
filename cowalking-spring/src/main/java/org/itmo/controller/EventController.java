// src/main/java/org/itmo/controller/EventController.java
package org.itmo.controller;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus;
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
    private final UserService userService;
    private final EventMapper eventMapper;

    // Основной список событий теперь перенаправляет на активные
    @GetMapping
    public String listEvents(Model model) {
        return listActiveEvents(model);
    }

    // Новый метод для активных событий
    @GetMapping("/active")
    public String listActiveEvents(Model model) {
        List<Event> activeEvents = eventService.findActiveEvents();
        List<EventDto> eventDtos = activeEvents.stream()
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());
        model.addAttribute("events", eventDtos);
        return "events/active-events";
    }

    // Новый метод для завершённых событий
    @GetMapping("/completed")
    public String listCompletedEvents(Model model) {
        List<Event> completedEvents = eventService.findCompletedEvents();
        List<EventDto> eventDtos = completedEvents.stream()
                .map(eventMapper::toEventDto)
                .collect(Collectors.toList());
        model.addAttribute("events", eventDtos);
        return "events/completed-events";
    }

    @GetMapping("/create")
    public String createEventForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        model.addAttribute("event", new EventDto());
        model.addAttribute("locations", locationService.findAll());
        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute EventDto eventDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        Event event = eventMapper.toEvent(eventDto);
        if (event.getId() == null) {
            event.setOrganizer(currentUser);
        }

        try {
            Event savedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + savedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Creation failed: " + e.getMessage());
            model.addAttribute("event", eventDto);
            model.addAttribute("locations", locationService.findAll());
            return "events/create";
        }
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto);
        return "events/details";
    }

    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto);
        model.addAttribute("locations", locationService.findAll());
        return "events/edit";
    }

    @PostMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, @ModelAttribute EventDto eventDto, Model model) {
        Event existingEvent = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        Event event = eventMapper.toEvent(eventDto);
        event.setId(id);
        event.setOrganizer(existingEvent.getOrganizer());

        try {
            Event updatedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + updatedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            model.addAttribute("event", eventDto);
            model.addAttribute("locations", locationService.findAll());
            return "events/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();

        eventService.deleteById(id, currentUser);
        return "redirect:/events";
    }
}