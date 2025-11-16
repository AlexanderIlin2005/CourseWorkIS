// src/main/java/org/itmo/controller/EventController.java
package org.itmo.controller;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus; // Импортируем enum
import org.itmo.service.EventService;
import org.itmo.service.LocationService;
import org.itmo.mapper.EventMapper; // Импортируем маппер
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
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private EventMapper eventMapper; // Внедряем маппер

    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventService.findAll();
        // Преобразуем список Event в список EventDto
        List<EventDto> eventDtos = events.stream()
                .map(eventMapper::toEventDto) // Используем маппер
                .collect(Collectors.toList());
        model.addAttribute("events", eventDtos); // Передаем DTO в модель
        return "events/list";
    }

    @GetMapping("/create")
    public String createEventForm(Model model) {
        model.addAttribute("event", new EventDto()); // Передаем DTO в модель
        model.addAttribute("locations", locationService.findAll());
        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute EventDto eventDto, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = (User) auth.getPrincipal();

        // Преобразуем DTO в сущность Event
        Event event = eventMapper.toEvent(eventDto);
        // Устанавливаем статус и текущего пользователя как организатора (если создается новый)
        if (event.getId() == null) {
            event.setStatus(EventStatus.ACTIVE); // Используем внешний enum
            event.setOrganizer(currentUser);
        }

        try {
            Event savedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + savedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // В случае ошибки, снова передаем DTO в модель
            model.addAttribute("event", eventDto);
            model.addAttribute("locations", locationService.findAll());
            return "events/create";
        }
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        // Преобразуем сущность Event в DTO для представления
        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto); // Передаем DTO в модель
        return "events/details";
    }

    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Check if user is organizer or admin
        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        // Преобразуем сущность Event в DTO для формы
        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto); // Передаем DTO в модель
        model.addAttribute("locations", locationService.findAll());
        return "events/edit";
    }

    @PostMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, @ModelAttribute EventDto eventDto, Model model) {
        Event existingEvent = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Check if user is organizer or admin
        if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        // Преобразуем DTO в сущность Event
        Event event = eventMapper.toEvent(eventDto);
        // Сохраняем ID и организатора из существующего события
        event.setId(id);
        event.setOrganizer(existingEvent.getOrganizer());

        try {
            Event updatedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + updatedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // В случае ошибки, снова передаем DTO в модель
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