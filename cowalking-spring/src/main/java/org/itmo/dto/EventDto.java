// src/main/java/org/itmo/dto/EventDto.java
package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; // Импортируем внешний enum
import lombok.Data;

import java.time.LocalDateTime; // Используем LocalDateTime
import java.time.format.DateTimeFormatter; // Для форматирования

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private String organizerUsername; // <-- Добавим поле для имени организатора
    private Long locationId;
    private String locationName; // <-- Добавим поле для имени локации
    private String locationAddress; // <-- Добавим поле для адреса локации
    // --- ИСПРАВЛЕНО: используем String для startTime и endTime ---
    private String startTime; // <-- Теперь строка
    private String endTime;   // <-- Теперь строка
    // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;

    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        // --- ИЗМЕНЕНО: сохраняем ID и имя организатора ---
        if (event.getOrganizer() != null) {
            this.organizerId = event.getOrganizer().getId();
            this.organizerUsername = event.getOrganizer().getUsername(); // <-- Сохраняем имя
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        // --- ИЗМЕНЕНО: сохраняем ID, имя и адрес локации ---
        if (event.getLocation() != null) {
            this.locationId = event.getLocation().getId();
            this.locationName = event.getLocation().getName(); // <-- Сохраняем имя
            this.locationAddress = event.getLocation().getAddress(); // <-- Сохраняем адрес
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        // --- ИСПРАВЛЕНО: преобразуем LocalDateTime в строку ---
        if (event.getStartTime() != null) {
            this.startTime = event.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")); // Формат datetime-local
        }
        if (event.getEndTime() != null) {
            this.endTime = event.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        }
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus(); // Теперь использует внешний enum
    }

    // --- МЕТОДЫ для преобразования строк обратно в LocalDateTime при сохранении ---
    public LocalDateTime getParsedStartTime() {
        if (startTime == null || startTime.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public LocalDateTime getParsedEndTime() {
        if (endTime == null || endTime.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
    // --- КОНЕЦ МЕТОДОВ ---
}