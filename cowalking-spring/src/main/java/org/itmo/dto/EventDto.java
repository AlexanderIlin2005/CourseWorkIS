// src/main/java/org/itmo/dto/EventDto.java
package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; // Импортируем enum
import lombok.Data;

import java.time.LocalDateTime; // <-- Используем LocalDateTime
import java.time.ZonedDateTime; // <-- Можем убрать, если не используется в DTO для других целей

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private Long locationId;
    private LocalDateTime startTime; // <-- Изменено на LocalDateTime
    private LocalDateTime endTime;   // <-- Изменено на LocalDateTime
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status; // Используем внешний enum

    // Конструкторы, если требуются
    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.organizerId = event.getOrganizer().getId();
        this.locationId = event.getLocation().getId();
        this.startTime = event.getStartTime(); // Теперь это LocalDateTime
        this.endTime = event.getEndTime();     // Теперь это LocalDateTime
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus();
    }
}