// src/main/java/org/itmo/model/Event.java
package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; // Импортируем конвертер
import org.itmo.model.converters.ZonedDateTimeConverter; // Импортируем конвертер
import org.itmo.model.enums.EventStatus; // <-- Добавьте этот импорт
import org.itmo.model.enums.UserRole; // Проверьте, что этот импорт тоже есть
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Convert(converter = ZonedDateTimeConverter.class)
    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Convert(converter = ZonedDateTimeConverter.class)
    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING) // <-- Убедитесь, что аннотация стоит
    private EventStatus status = EventStatus.ACTIVE; // <-- Используем импортированный enum
}