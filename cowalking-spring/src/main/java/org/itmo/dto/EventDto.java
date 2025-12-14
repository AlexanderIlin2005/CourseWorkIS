package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; // Импортируем внешний enum
import lombok.Data;

import java.time.LocalDateTime; // Используем LocalDateTime
import java.time.format.DateTimeFormatter;

import org.itmo.model.enums.EventDifficulty; // <-- Добавлен импорт


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
    // --- ИСПРАВЛЕНО: используем LocalDateTime для startTime и endTime ---
    private LocalDateTime startTime; // <-- Теперь LocalDateTime
    private LocalDateTime endTime;   // <-- Теперь LocalDateTime
    // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;

    private Long eventTypeId; // <-- Добавлено
    private String eventTypeName; // <-- Добавлено для отображения
    private EventDifficulty difficulty; // <-- Добавлено

    // --- ДОБАВЛЕНО: Продолжительность в минутах ---
    private Long durationMinutes;
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    private Double averageRating;

    // --- ДОБАВЛЕНО: Количество отзывов ---
    private Long reviewCount;
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    // --- ДОБАВЛЕНО: URL фотографии мероприятия ---
    private String photoUrl;
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    private String organizerPhotoUrl; // <-- Добавить это поле


    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        // --- ИЗМЕНЕНО: сохраняем ID и имя организатора ---
        if (event.getOrganizer() != null) {
            this.organizerId = event.getOrganizer().getId();
            this.organizerUsername = event.getOrganizer().getUsername(); // <-- Сохраняем имя
            this.organizerPhotoUrl = event.getOrganizer().getPhotoUrl(); // <-- Заполнить это поле
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        // --- ИЗМЕНЕНО: сохраняем ID, имя и адрес локации ---
        if (event.getLocation() != null) {
            this.locationId = event.getLocation().getId();
            this.locationName = event.getLocation().getName(); // <-- Сохраняем имя
            this.locationAddress = event.getLocation().getAddress(); // <-- Сохраняем адрес
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        // --- ИЗМЕНЕНО: сохраняем LocalDateTime ---
        this.startTime = event.getStartTime(); // <-- Теперь LocalDateTime
        this.endTime = event.getEndTime();     // <-- Теперь LocalDateTime
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus(); // Теперь использует внешний enum

        // --- ДОБАВЛЕНО: Маппинг типа и сложности ---
        if (event.getEventType() != null) {
            this.eventTypeId = event.getEventType().getId();
            this.eventTypeName = event.getEventType().getName();
        }
        this.difficulty = event.getDifficulty();
        // --- КОНЕЦ ДОБАВЛЕНИЯ ---

        // --- ДОБАВЛЕНО: Расчет продолжительности ---
        if (event.getStartTime() != null && event.getEndTime() != null) {
            this.durationMinutes = java.time.Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
        }
        // --- КОНЕЦ ДОБАВЛЕНИЯ ---

        this.averageRating = event.getAverageRating();

        this.photoUrl = event.getPhotoUrl();
    }
}
