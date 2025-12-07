// src/main/java/org/itmo/model/Event.java
package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; // Импортируем конвертер
import org.itmo.model.enums.EventStatus; // Импортируем enum
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime; // <-- Используем LocalDateTime
import java.util.Collection;
import java.util.Collections;

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

    @Column(name = "created_at")
    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер для createdAt
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер для updatedAt
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.ACTIVE; // Используем внешний enum
}