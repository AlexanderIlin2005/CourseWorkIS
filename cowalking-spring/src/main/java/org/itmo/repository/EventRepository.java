package org.itmo.repository;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; // Импортируем внешний enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByLocationId(Long locationId);
    List<Event> findByStatus(EventStatus status); // <-- Используем внешний enum
}
