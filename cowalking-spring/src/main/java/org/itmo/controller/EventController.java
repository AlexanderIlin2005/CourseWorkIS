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
import org.itmo.service.UserService;
import org.itmo.service.FileStorageService;
import org.itmo.mapper.EventMapper;
import org.itmo.repository.EventTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.itmo.model.enums.EventDifficulty;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import java.util.List;

import org.itmo.service.ReviewService;

import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final EventTypeRepository eventTypeRepository;
    private final ReviewService reviewService;
    private final FileStorageService fileStorageService;

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
    public String createEventForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        model.addAttribute("event", new EventDto());
        model.addAttribute("eventTypes", eventTypeRepository.findAll());
        model.addAttribute("difficulties", Arrays.asList(EventDifficulty.values()));

        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("eventTypeId") Long eventTypeId,
            @RequestParam("difficulty") EventDifficulty difficulty,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime,
            @RequestParam("startAddress") String startAddress,
            @RequestParam("endAddress") String endAddress,
            @RequestParam(value = "maxParticipants", required = false) Integer maxParticipants,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile,
            @RequestParam(value = "startLatitude", required = false) Double startLatitude,
            @RequestParam(value = "startLongitude", required = false) Double startLongitude,
            Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        try {
            // Создаем новое событие
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setStartTime(startTime);
            event.setEndTime(endTime);
            event.setMaxParticipants(maxParticipants);
            event.setDifficulty(difficulty);
            event.setOrganizer(currentUser);
            event.setStartAddress(startAddress);
            event.setEndAddress(endAddress);

            // --- ДОБАВЛЕНО: Установка координат ---
            event.setStartLatitude(startLatitude);
            event.setStartLongitude(startLongitude);
            // --- КОНЕЦ ДОБАВЛЕНИЯ ---

            // Обработка загрузки фотографии
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    String photoPath = fileStorageService.storeFile(photoFile, "event");
                    event.setPhotoUrl("/" + photoPath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store event photo", e);
                }
            }

            // Установка связи с EventType
            EventType eventType = eventTypeRepository.findById(eventTypeId)
                    .orElseThrow(() -> new RuntimeException("Event type not found with id: " + eventTypeId));
            event.setEventType(eventType);

            // Сохранение события
            Event savedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + savedEvent.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Creation failed: " + e.getMessage());

            // Заполняем DTO для отображения данных в форме в случае ошибки
            EventDto errorDto = new EventDto();
            errorDto.setTitle(title);
            errorDto.setDescription(description);
            errorDto.setEventTypeId(eventTypeId);
            errorDto.setDifficulty(difficulty);
            errorDto.setStartTime(startTime);
            errorDto.setEndTime(endTime);
            errorDto.setStartAddress(startAddress);
            errorDto.setEndAddress(endAddress);
            errorDto.setMaxParticipants(maxParticipants);

            model.addAttribute("event", errorDto);
            model.addAttribute("eventTypes", eventTypeRepository.findAll());
            model.addAttribute("difficulties", Arrays.asList(EventDifficulty.values()));
            return "events/create";
        }
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable Long id, Model model, Authentication authentication) {
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
    public String editEventForm(@PathVariable Long id, Model model, Authentication authentication) {
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
        model.addAttribute("eventTypes", eventTypeRepository.findAll());
        model.addAttribute("difficulties", Arrays.asList(EventDifficulty.values()));

        return "events/edit";
    }

    @PostMapping("/{id}/edit")
    public String editEvent(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("eventTypeId") Long eventTypeId,
            @RequestParam("difficulty") EventDifficulty difficulty,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime,
            @RequestParam("startAddress") String startAddress,
            @RequestParam("endAddress") String endAddress,
            @RequestParam(value = "maxParticipants", required = false) Integer maxParticipants,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile,
            @RequestParam(value = "startLatitude", required = false) Double startLatitude,
            @RequestParam(value = "startLongitude", required = false) Double startLongitude,
            Model model, Authentication authentication) {

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

        try {
            // Создаем объект Event для обновления
            Event event = new Event();
            event.setId(id);
            event.setTitle(title);
            event.setDescription(description);
            event.setDifficulty(difficulty);
            event.setStartTime(startTime);
            event.setEndTime(endTime);
            event.setMaxParticipants(maxParticipants);
            event.setOrganizer(existingEvent.getOrganizer());
            event.setStartAddress(startAddress);
            event.setEndAddress(endAddress);

            // --- ДОБАВЛЕНО: Установка координат ---
            event.setStartLatitude(startLatitude);
            event.setStartLongitude(startLongitude);
            // --- КОНЕЦ ДОБАВЛЕНИЯ ---

            // Обработка загрузки фотографии
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    String photoPath = fileStorageService.storeFile(photoFile, "event");
                    event.setPhotoUrl("/" + photoPath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store event photo", e);
                }
            } else {
                // Сохраняем существующую фотографию, если новая не загружена
                event.setPhotoUrl(existingEvent.getPhotoUrl());
            }

            // Установка связи с EventType
            EventType eventType = eventTypeRepository.findById(eventTypeId)
                    .orElseThrow(() -> new RuntimeException("Event type not found with id: " + eventTypeId));
            event.setEventType(eventType);

            Event updatedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + updatedEvent.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());

            // Заполняем DTO для отображения данных в форме в случае ошибки
            EventDto errorDto = new EventDto();
            errorDto.setId(id);
            errorDto.setTitle(title);
            errorDto.setDescription(description);
            errorDto.setEventTypeId(eventTypeId);
            errorDto.setDifficulty(difficulty);
            errorDto.setStartTime(startTime);
            errorDto.setEndTime(endTime);
            errorDto.setStartAddress(startAddress);
            errorDto.setEndAddress(endAddress);
            errorDto.setMaxParticipants(maxParticipants);
            errorDto.setPhotoUrl(existingEvent.getPhotoUrl());

            // --- ГЛАВНОЕ ИСПРАВЛЕНИЕ: Заполняем координаты из параметров формы ---
            errorDto.setStartLatitude(startLatitude);
            errorDto.setStartLongitude(startLongitude);
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

            model.addAttribute("event", errorDto);
            model.addAttribute("eventTypes", eventTypeRepository.findAll());
            model.addAttribute("difficulties", Arrays.asList(EventDifficulty.values()));
            return "events/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }
        User currentUser = (User) authentication.getPrincipal();

        eventService.deleteById(id, currentUser);
        return "redirect:/events";
    }
}