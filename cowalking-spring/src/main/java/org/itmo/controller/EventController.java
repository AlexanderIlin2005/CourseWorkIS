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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final LocationService locationService;
    private final EventMapper eventMapper; // Используем маппер

    @Autowired
    private UserService userService; // <-- Внедряем UserService

    @GetMapping
    public String listEvents(Model model) {
        List<Event> events = eventService.findAll();
        // Преобразуем список Event в список EventDto
        List<EventDto> eventDtos = events.stream()
                .map(eventMapper::toEventDto) // Используем маппер
                .collect(java.util.stream.Collectors.toList());
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
        // --- ИМИТАЦИЯ: Получаем текущего пользователя ---
        // В реальности нужно хранить ID/имя пользователя в сессии или cookies
        // Пока используем фиктивного пользователя, например, admin
        User fakeCurrentUser = userService.findByUsername("admin") // Попробуем найти админа
                .orElseThrow(() -> new RuntimeException("Fake user 'admin' not found for demo purposes."));
        // --- КОНЕЦ ИМИТАЦИИ ---

        // Преобразуем DTO в сущность Event
        Event event = eventMapper.toEvent(eventDto);
        if (event.getId() == null) { // Если создается новый
            event.setStatus(EventStatus.ACTIVE); // Используем внешний enum
            event.setOrganizer(fakeCurrentUser); // Устанавливаем фиктивного организатора
        }

        try {
            Event savedEvent = eventService.save(event, fakeCurrentUser);
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

        // --- ИМИТАЦИЯ: Проверка прав ---
        // В реальности нужно проверить, является ли текущий пользователь организатором или админом
        // Пока просто проверим, что текущий пользователь - admin
        User fakeCurrentUser = userService.findByUsername("admin") // Попробуем найти админа
                .orElseThrow(() -> new RuntimeException("Fake user 'admin' not found for demo purposes."));

        if (!event.getOrganizer().getId().equals(fakeCurrentUser.getId())) {
            return "redirect:/events/" + id; // Не организатор - не редактирует
        }
        // --- КОНЕЦ ИМИТАЦИИ ---

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

        // --- ИМИТАЦИЯ: Проверка прав ---
        // В реальности нужно проверить, является ли текущий пользователь организатором или админом
        // Пока просто проверим, что текущий пользователь - admin
        User fakeCurrentUser = userService.findByUsername("admin") // Попробуем найти админа
                .orElseThrow(() -> new RuntimeException("Fake user 'admin' not found for demo purposes."));

        if (!existingEvent.getOrganizer().getId().equals(fakeCurrentUser.getId())) {
            return "redirect:/events/" + id; // Не организатор - не редактирует
        }
        // --- КОНЕЦ ИМИТАЦИИ ---

        // Преобразуем DTO в сущность Event
        Event event = eventMapper.toEvent(eventDto);
        // --- ИСПРАВЛЕНО: Сохраняем ID, организатора и currentParticipants из существующего события ---
        event.setId(id);
        event.setOrganizer(existingEvent.getOrganizer()); // Сохраняем оригинального организатора
        event.setCurrentParticipants(existingEvent.getCurrentParticipants()); // <-- Сохраняем количество участников
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
        // Не сохраняем status, если не предполагается его изменение через редактирование
        // event.setStatus(existingEvent.getStatus()); // <-- Опционально, если status не редактируется

        try {
            Event updatedEvent = eventService.save(event, fakeCurrentUser);
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

        // --- ИМИТАЦИЯ: Проверка прав ---
        User fakeCurrentUser = userService.findByUsername("admin") // Попробуем найти админа
                .orElseThrow(() -> new RuntimeException("Fake user 'admin' not found for demo purposes."));

        if (!event.getOrganizer().getId().equals(fakeCurrentUser.getId())) {
            return "redirect:/events/" + id; // Не организатор - не удаляет
        }
        // --- КОНЕЦ ИМИТАЦИИ ---

        eventService.deleteById(id, fakeCurrentUser);
        return "redirect:/events";
    }
}