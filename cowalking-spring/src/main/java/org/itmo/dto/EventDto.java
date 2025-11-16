// src/main/java/org/itmo/dto/EventDto.java
package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; // Импортируем enum
import lombok.Data;

import java.time.ZonedDateTime; // Используем ZonedDateTime

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId; // Может быть использовано для отправки ID организатора при создании
    private Long locationId;  // Используется для выбора локации
    private ZonedDateTime startTime; // Обновлено с LocalDateTime на ZonedDateTime
    private ZonedDateTime endTime;   // Обновлено с LocalDateTime на ZonedDateTime
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status; // Обновлено: используем внешний enum

    public EventDto() {}

    // Конструктор из сущности Event
    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.organizerId = event.getOrganizer().getId();
        this.locationId = event.getLocation().getId();
        this.startTime = event.getStartTime(); // Теперь ZonedDateTime
        this.endTime = event.getEndTime();     // Теперь ZonedDateTime
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus(); // Теперь использует внешний enum
    }
}