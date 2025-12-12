// src/main/java/org/itmo/controller/EventController.java
package org.itmo.controller;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus; // Импортируем внешний enum
import org.itmo.service.EventService;
import org.itmo.service.LocationService;
import org.itmo.service.UserService; // Импортируем UserService для получения текущего пользователя
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
    private final UserService userService; // <-- Внедряем UserService для получения текущего пользователя
    private final EventMapper eventMapper; // <-- Внедряем EventMapper

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
        // Проверяем права: только ADMIN или ORGANIZER могут создавать
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return "redirect:/login";
        }
        User currentUser = (User) auth.getPrincipal();
        //if (!currentUser.getRole().equals(org.itmo.model.enums.UserRole.ORGANIZER) &&
        //        !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
        //    return "redirect:/events";
        //}

        model.addAttribute("event", new EventDto()); // Передаем DTO в модель
        model.addAttribute("locations", locationService.findAll());
        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute EventDto eventDto, Model model) {
        // --- ИСПРАВЛЕНО: проверка только аутентификации (уже сделана в SecurityFilterChain) ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        // Преобразуем DTO в сущность Event
        Event event = eventMapper.toEvent(eventDto);
        if (event.getId() == null) { // Если создается новый
            event.setStatus(EventStatus.ACTIVE); // Используем внешний enum
            event.setOrganizer(currentUser); // Устанавливаем текущего пользователя как организатора
        }

        try {
            Event savedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + savedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Creation failed: " + e.getMessage());
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

        // --- ИСПРАВЛЕНО: проверка прав: только организатор или админ может редактировать ---
        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id; // Не организатор и не админ - не редактирует
        }
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

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

        // --- ИСПРАВЛЕНО: проверка прав: только организатор или админ может редактировать ---
        if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id; // Не организатор и не админ - не редактирует
        }
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        // Преобразуем DTO в сущность Event
        Event event = eventMapper.toEvent(eventDto);
        // Сохраняем ID и организатора из существующего события
        event.setId(id);
        event.setOrganizer(existingEvent.getOrganizer());

        try {
            Event updatedEvent = eventService.save(event, currentUser);
            return "redirect:/events/" + updatedEvent.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            // В случае ошибки, снова передаем DTO в модель
            model.addAttribute("event", eventDto);
            model.addAttribute("locations", locationService.findAll());
            return "events/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // --- ИСПРАВЛЕНО: проверка прав: только организатор или админ может удалить ---
        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(org.itmo.model.enums.UserRole.ADMIN)) {
            return "redirect:/events/" + id; // Не организатор и не админ - не удаляет
        }
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        eventService.deleteById(id, currentUser);
        return "redirect:/events";
    }
}