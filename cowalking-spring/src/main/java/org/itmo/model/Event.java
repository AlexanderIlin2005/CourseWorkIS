package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; // Импортируем конвертер
import org.itmo.model.enums.EventStatus; // Импортируем внешний enum
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Используем LocalDateTime

import org.itmo.model.enums.EventDifficulty; // <-- Добавлен импорт

@Entity
@Table(name = "cowalking_events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // --- УДАЛЕНО: Связь с Location ---
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "location_id", nullable = false)
    // private Location location;
    // --- КОНЕЦ УДАЛЕНИЯ ---

    // --- ИСПОЛЬЗУЕМ LocalDateTime с конвертером ---
    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // <-- Изменено на LocalDateTime

    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // <-- Изменено на LocalDateTime
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер для createdAt
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер для updatedAt
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- ИСПРАВЛЕНО: используем внешний enum ---
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.ACTIVE; // <-- Используем внешний enum
    // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id")
    private EventType eventType; // <-- Добавлено

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private EventDifficulty difficulty; // <-- Добавлено

    @Column(name = "average_rating")
    private Double averageRating; // Средний рейтинг мероприятия

    // --- ДОБАВЛЕНО: URL фотографии мероприятия ---
    @Column(length = 500)
    private String photoUrl;
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    // --- ДОБАВЛЕНО: Текстовые адреса ---
    @Column(name = "start_address", length = 500, nullable = false)
    private String startAddress;

    @Column(name = "end_address", length = 500)
    private String endAddress;
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

}
