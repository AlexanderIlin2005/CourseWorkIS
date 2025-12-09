package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; // Импортируем конвертер
import org.itmo.model.enums.ParticipationStatus; // Импортируем внешний enum
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Используем LocalDateTime

@Entity
@Table(name = "cowalking_participations")
@Getter
@Setter
@NoArgsConstructor
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // --- ИСПОЛЬЗУЕМ LocalDateTime с конвертером ---
    @Convert(converter = LocalDateTimeConverter.class) // <-- Добавляем конвертер
    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    @Column(name = "status")
    @Enumerated(EnumType.STRING) // <-- Используем внешний enum
    private ParticipationStatus status = ParticipationStatus.PENDING; // <-- Используем внешний enum
}
