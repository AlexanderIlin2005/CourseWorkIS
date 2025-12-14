package org.itmo.repository;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.itmo.model.enums.EventDifficulty;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByLocationId(Long locationId);
    //List<Event> findByStatus(EventStatus status); // <-- Используем внешний enum
    // --- НОВЫЙ МЕТОД: Загружаем все связи ---
    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.organizer " +
            "JOIN FETCH e.location " +
            "LEFT JOIN FETCH e.eventType " + // LEFT JOIN, так как тип может быть null
            "WHERE e.status = :status")
    List<Event> findEventsWithAllDetailsByStatus(@Param("status") EventStatus status);

    // --- НОВЫЙ МЕТОД: Для фильтрации по типу ---
    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.organizer " +
            "JOIN FETCH e.location " +
            "LEFT JOIN FETCH e.eventType " +
            "WHERE e.status = :status AND e.eventType.id = :eventTypeId")
    List<Event> findEventsWithAllDetailsByStatusAndEventType(
            @Param("status") EventStatus status,
            @Param("eventTypeId") Long eventTypeId);

    // --- НОВЫЙ МЕТОД: Для фильтрации по сложности ---
    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.organizer " +
            "JOIN FETCH e.location " +
            "LEFT JOIN FETCH e.eventType " +
            "WHERE e.status = :status AND e.difficulty = :difficulty")
    List<Event> findEventsWithAllDetailsByStatusAndDifficulty(
            @Param("status") EventStatus status,
            @Param("difficulty") EventDifficulty difficulty);

    // --- НОВЫЙ МЕТОД: Для фильтрации по типу И сложности ---
    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.organizer " +
            "JOIN FETCH e.location " +
            "LEFT JOIN FETCH e.eventType " +
            "WHERE e.status = :status AND e.eventType.id = :eventTypeId AND e.difficulty = :difficulty")
    List<Event> findEventsWithAllDetailsByStatusEventTypeAndDifficulty(
            @Param("status") EventStatus status,
            @Param("eventTypeId") Long eventTypeId,
            @Param("difficulty") EventDifficulty difficulty);
}
