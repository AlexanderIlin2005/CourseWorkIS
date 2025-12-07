// src/main/java/org/itmo/dto/EventDto.java
package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    // --- ИЗМЕНЕНО: добавлено locationName, убрано location ---
    private String locationName; // <-- Добавлено поле для имени локации
    private Long locationId;     // <-- Оставлено ID локации
    private String locationAddress; // <-- Добавить
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;

    // Конструкторы
    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.organizerId = event.getOrganizer().getId();
        // --- ИЗМЕНЕНО: установка locationName и locationId ---
        if (event.getLocation() != null) { // Проверка на null
            this.locationName = event.getLocation().getName(); // Устанавливаем имя локации
            this.locationId = event.getLocation().getId();     // Устанавливаем ID локации
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus();
    }
}