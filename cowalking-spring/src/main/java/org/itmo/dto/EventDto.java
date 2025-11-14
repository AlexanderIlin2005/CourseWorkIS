// src/main/java/org/itmo/dto/EventDto.java
package org.itmo.dto;

import org.itmo.model.Event; // Импортируем Event, чтобы знать о его внутреннем enum
import org.itmo.model.enums.EventStatus; // Или импортируем сам enum, если он в отдельном файле
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private Long locationId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status; // <-- Теперь тип известен

    // Конструкторы
    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.organizerId = event.getOrganizer().getId();
        this.locationId = event.getLocation().getId();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus(); // <-- Получаем статус
    }
}