// src/main/java/org/itmo/model/Participation.java
package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; // Импортируем конвертер
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING) // <-- Убедитесь, что аннотация стоит
    private ParticipationStatus status = ParticipationStatus.PENDING; // <-- Предполагается, что enum определен

    // Определение enum внутри класса
    public enum ParticipationStatus {
        PENDING, CONFIRMED, CANCELLED
    }
}