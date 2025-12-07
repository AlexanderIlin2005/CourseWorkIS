// src/main/java/org/itmo/dto/EventDto.java
package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus;
import lombok.Data;

import java.time.LocalDateTime; // Используем LocalDateTime

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    // --- ИЗМЕНЕНО: добавлен organizerUsername, убран organizer ---
    private String organizerUsername; // <-- Добавлено поле для имени организатора
    private Long organizerId;         // <-- Оставлено ID организатора
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    // --- ИЗМЕНЕНО: добавлены locationName и locationAddress, убран location ---
    private String locationName;      // <-- Добавлено поле для имени локации
    private String locationAddress;   // <-- Добавлено поле для адреса локации
    private Long locationId;          // <-- Оставлено ID локации
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    private LocalDateTime startTime; // Теперь LocalDateTime
    private LocalDateTime endTime;   // Теперь LocalDateTime
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;

    // Конструкторы
    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        // --- ИЗМЕНЕНО: установка organizerUsername и organizerId ---
        if (event.getOrganizer() != null) { // Проверка на null
            this.organizerUsername = event.getOrganizer().getUsername(); // Устанавливаем имя организатора
            this.organizerId = event.getOrganizer().getId();             // Устанавливаем ID организатора
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        // --- ИЗМЕНЕНО: установка locationName, locationAddress и locationId ---
        if (event.getLocation() != null) { // Проверка на null
            this.locationName = event.getLocation().getName();      // Устанавливаем имя локации
            this.locationAddress = event.getLocation().getAddress(); // Устанавливаем адрес локации
            this.locationId = event.getLocation().getId();          // Устанавливаем ID локации
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus();
    }
}