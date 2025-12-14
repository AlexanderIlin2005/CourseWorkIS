// src/main/java/org/itmo/service/EventService.java
package org.itmo.service;

import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.stream.Collectors; // <-- Импортируем Collectors

import org.itmo.util.MoscowTimeUtil; // Импортируем утилиту

import org.itmo.model.Event;
import org.itmo.model.enums.EventDifficulty;

//import org.springframework.data.domain.Specification;
//import jakarta.persistence.criteria.Join;
//import jakarta.persistence.criteria.Predicate;


@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findByOrganizerId(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    public List<Event> findByStatus(EventStatus status) { // <-- Используйте импортированный enum
        return eventRepository.findByStatus(status);
    }

    @Transactional
    public Event save(Event event, User currentUser) {
        // Проверка логики: startTime < endTime
        if (event.getStartTime() != null && event.getEndTime() != null && event.getStartTime().isAfter(event.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Проверка: startTime не может быть в прошлом (по Московскому времени)
        LocalDateTime currentMoscowTime = MoscowTimeUtil.getCurrentMoscowTime();
        if (event.getStartTime() != null && event.getStartTime().isBefore(currentMoscowTime.minusMinutes(1))) {
            throw new IllegalArgumentException("Start time cannot be in the past.");
        }

        if (event.getId() == null) {
            event.setOrganizer(currentUser);
            // Проверка: endTime должна быть после startTime на момент создания
            if (event.getEndTime() != null && !event.getEndTime().isAfter(event.getStartTime())) {
                throw new IllegalArgumentException("End time must be after start time.");
            }
            event.setStatus(EventStatus.ACTIVE);
        } else {
            // При обновлении события, проверяем, что статус не меняется на COMPLETED вручную
            // и что нельзя изменить endTime завершённого события
            Event existingEvent = eventRepository.findById(event.getId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            if (EventStatus.COMPLETED.equals(existingEvent.getStatus())) {
                // Нельзя редактировать завершённое событие
                // Можем бросить исключение или игнорировать изменения endTime/status
                throw new SecurityException("Cannot edit a completed event.");
            }
            // Проверка: endTime должна быть после startTime
            if (event.getEndTime() != null && !event.getEndTime().isAfter(event.getStartTime())) {
                throw new IllegalArgumentException("End time must be after start time.");
            }
            // Сохраняем оригинальный организатора
            event.setOrganizer(existingEvent.getOrganizer());
        }

        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteById(Long id, User currentUser) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Проверка прав и статуса
        if (EventStatus.COMPLETED.equals(event.getStatus())) {
            throw new SecurityException("Cannot delete a completed event.");
        }
        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new SecurityException("You can only delete active events you organized.");
        }

        eventRepository.deleteById(id);
    }

    public List<Event> findAllSorted(String sort, String direction) {
        // Базовая проверка параметров
        if (!"startTime".equals(sort) && !"endTime".equals(sort)) {
            sort = "startTime"; // Значение по умолчанию
        }
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            direction = "asc"; // Значение по умолчанию
        }

        // Используем Spring Data JPA для сортировки
        Sort sortDirection = "asc".equals(direction) ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        return eventRepository.findAll(sortDirection);
    }

    // Новый метод для получения активных событий
    public List<Event> findActiveEvents() {
        List<Event> allEvents = eventRepository.findAll();
        LocalDateTime currentMoscowTime = MoscowTimeUtil.getCurrentMoscowTime();
        return allEvents.stream()
                .filter(event -> {
                    // Обновляем статус события, если необходимо
                    updateEventStatusIfNeeded(event);
                    // Фильтруем только ACTIVE события
                    return EventStatus.ACTIVE.equals(event.getStatus());
                })
                .collect(Collectors.toList());
    }

    // Новый метод для получения завершённых событий
    public List<Event> findCompletedEvents() {
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .filter(event -> {
                    // Обновляем статус события, если необходимо
                    updateEventStatusIfNeeded(event);
                    // Фильтруем только COMPLETED события
                    return EventStatus.COMPLETED.equals(event.getStatus());
                })
                .collect(Collectors.toList());
    }

    // Новый метод для обновления статуса события
    public void updateEventStatusIfNeeded(Event event) {
        if (EventStatus.ACTIVE.equals(event.getStatus()) &&
                MoscowTimeUtil.isEventCompleted(event.getEndTime())) {
            event.setStatus(EventStatus.COMPLETED);
            // Обновляем только статус и updated_at
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event); // Сохраняем обновление статуса
        }
    }


    // --- ЗАМЕНИТЬ МЕТОД findActiveEventsFiltered ---
    /**
     * Находит активные события с фильтрацией по типу, сложности и продолжительности.
     * Фильтрация происходит на стороне Java после получения всех активных событий.
     */
    public List<Event> findActiveEventsFiltered(
            Long eventTypeId,
            EventDifficulty difficulty,
            Integer minDurationMinutes,
            Integer maxDurationMinutes) {

        // Сначала получаем все активные события (с обновлением статуса)
        List<Event> activeEvents = this.findActiveEvents();

        return activeEvents.stream()
                .filter(event -> {
                    // Фильтр по типу события
                    boolean typeOk = (eventTypeId == null) ||
                            (event.getEventType() != null && eventTypeId.equals(event.getEventType().getId()));
                    // Фильтр по сложности
                    boolean difficultyOk = (difficulty == null) || difficulty.equals(event.getDifficulty());
                    return typeOk && difficultyOk;
                })
                .filter(event -> {
                    // Фильтр по продолжительности
                    if (minDurationMinutes == null && maxDurationMinutes == null) {
                        return true; // Нет фильтра по длительности
                    }
                    if (event.getStartTime() == null || event.getEndTime() == null) {
                        return false; // Событие без времени не подходит
                    }
                    long durationMinutes = java.time.Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
                    boolean minOk = (minDurationMinutes == null) || (durationMinutes >= minDurationMinutes);
                    boolean maxOk = (maxDurationMinutes == null) || (durationMinutes <= maxDurationMinutes);
                    return minOk && maxOk;
                })
                .collect(Collectors.toList());
    }
    // --- КОНЕЦ ЗАМЕНЫ ---


}