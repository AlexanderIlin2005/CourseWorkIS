// src/main/java/org/itmo/controller/EventController.java
package org.itmo.controller;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus;
import org.itmo.model.EventType;
import org.itmo.model.Review;
import org.itmo.dto.ReviewDto;
import org.itmo.service.EventService;
import org.itmo.service.LocationService;
import org.itmo.service.UserService;
import org.itmo.mapper.EventMapper;
import org.itmo.repository.EventTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import org.itmo.model.enums.EventDifficulty;

import java.util.Arrays;
import java.util.stream.Collectors;

import java.util.List;

import org.itmo.service.ReviewService; // <-- Импортируем ReviewService

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final LocationService locationService;
    private final UserService userService; // <-- УЖЕ ЕСТЬ
    private final EventMapper eventMapper;
    private final EventTypeRepository eventTypeRepository; // <-- Внедряем репозиторий типов
    private final ReviewService reviewService; // <-- Внедряем ReviewService

    @GetMapping
    public String listEvents(Model model) {
        return listActiveEvents(model, null, null, null, null);
    }

    // --- ЕДИНСТВЕННЫЙ МЕТОД ДЛЯ /active ---
    @GetMapping("/active")
    public String listActiveEvents(
            Model model,
            @RequestParam(required = false) Long eventTypeId,
            @RequestParam(required = false) EventDifficulty difficulty,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration) {

        List<Event> activeEvents = eventService.findActiveEventsFiltered(
                eventTypeId, difficulty, minDuration, maxDuration);

        List<EventDto> eventDtos = activeEvents.stream()
                .map(event -> {
                    EventDto dto = eventMapper.toEventDto(event);
                    // --- ДОБАВЛЕНО: Заполнение количества отзывов ---
                    dto.setReviewCount(eventService.getReviewCountForEvent(event.getId()));
                    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
                    return dto;
                })
                .collect(Collectors.toList());

        model.addAttribute("eventTypes", eventTypeRepository.findAll());
        model.addAttribute("selectedEventTypeId", eventTypeId);
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("selectedMinDuration", minDuration);
        model.addAttribute("selectedMaxDuration", maxDuration);
        model.addAttribute("events", eventDtos);
        return "events/active-events";
    }
    // --- КОНЕЦ ЕДИНСТВЕННОГО МЕТОДА ---

    // Новый метод для завершённых событий
    @GetMapping("/completed")
    public String listCompletedEvents(Model model) {
        List<Event> completedEvents = eventService.findCompletedEvents();
        List<EventDto> eventDtos = completedEvents.stream()
                .map(event -> {
                    EventDto dto = eventMapper.toEventDto(event);
                    dto.setReviewCount(eventService.getReviewCountForEvent(event.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        model.addAttribute("events", eventDtos);
        return "events/completed-events";
    }

    @GetMapping("/create")
    public String createEventForm(Model model, Authentication authentication) { // <-- ДОБАВЬТЕ ПАРАМЕТР
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        model.addAttribute("event", new EventDto());
        model.addAttribute("locations", locationService.findAll());

        model.addAttribute("eventTypes", eventTypeRepository.findAll()); // <-- Передаем типы в форму создания
        model.addAttribute("difficulties", Arrays.asList(EventDifficulty.values())); // <-- Передаем сложности

        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute EventDto eventDto, Model model, Authentication authentication) { // <-- ДОБАВЬТЕ ПАРАМЕТР
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        // --- ИСПРАВЛЕНО: Создаем переменную event ---
        Event event = eventMapper.toEvent(eventDto);
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---


        if (event.getId() == null) {
            event.setOrganizer(currentUser);
        }


        try {

            // --- ИСПРАВЛЕНО: Добавление типа и сложности ---
            if (eventDto.getEventTypeId() != null) {
                EventType eventType = eventTypeRepository.findById(eventDto.getEventTypeId())
                        .orElseThrow(() -> new RuntimeException("Event type not found"));
                event.setEventType(eventType);
            }
            event.setDifficulty(eventDto.getDifficulty());
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

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
    public String eventDetails(@PathVariable Long id, Model model, Authentication authentication) { // <-- ДОБАВЬТЕ ПАРАМЕТР
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto);

        // Загружаем отзывы для события
        List<Review> reviews = reviewService.getReviewsForEvent(id);
        model.addAttribute("reviews", reviews);

        // Загружаем DTO для формы отзыва
        model.addAttribute("reviewDto", new ReviewDto());

        // Добавляем ID текущего пользователя, если он аутентифицирован
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            model.addAttribute("currentUserId", currentUser.getId());
        } else {
            model.addAttribute("currentUserId", null);
        }

        return "events/details";
    }

    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model, Authentication authentication) { // <-- ДОБАВЬТЕ ПАРАМЕТР
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }

        EventDto eventDto = eventMapper.toEventDto(event);
        model.addAttribute("event", eventDto);
        model.addAttribute("locations", locationService.findAll());

        model.addAttribute("eventTypes", eventTypeRepository.findAll());
        model.addAttribute("difficulties", Arrays.asList(EventDifficulty.values()));

        return "events/edit";
    }

    @PostMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, @ModelAttribute EventDto eventDto, Model model, Authentication authentication) { // <-- ДОБАВЬТЕ ПАРАМЕТР
        Event existingEvent = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id;
        }



        Event event = eventMapper.toEvent(eventDto);
        event.setId(id);
        event.setOrganizer(existingEvent.getOrganizer());

        try {

            // --- ИСПРАВЛЕНО: Добавление типа и сложности ---
            if (eventDto.getEventTypeId() != null) {
                EventType eventType = eventTypeRepository.findById(eventDto.getEventTypeId())
                        .orElseThrow(() -> new RuntimeException("Event type not found"));
                event.setEventType(eventType);
            }
            event.setDifficulty(eventDto.getDifficulty());
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

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
    public String deleteEvent(@PathVariable Long id, Authentication authentication) { // <-- ДОБАВЬТЕ ПАРАМЕТР
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        eventService.deleteById(id, currentUser);
        return "redirect:/events";
    }
}