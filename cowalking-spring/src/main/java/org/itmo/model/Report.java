// src/main/java/org/itmo/model/Report.java
package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; // Импортируем конвертер
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cowalking_reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false, length = 1000)
    private String reason;

    // --- Используем кастомный конвертер ---
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    // --- Конец изменений ---

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;
}