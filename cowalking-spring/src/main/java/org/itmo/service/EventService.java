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

        // Если создаем новый, устанавливаем организатора и статус
        if (event.getId() == null) {
            event.setOrganizer(currentUser);
            event.setStatus(EventStatus.ACTIVE); // Используем внешний enum
        }
        // Обновляем время обновления
        event.setUpdatedAt(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Transactional
    public void deleteById(Long id, User currentUser) {
        // Проверка прав: только организатор или админ может удалить
        // Эта проверка теперь должна быть в контроллере, до вызова service
        // if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
        //     !currentUser.getRole().equals(UserRole.ADMIN)) {
        //     throw new SecurityException("You can only delete events you organized");
        // }

        eventRepository.deleteById(id);
    }
}