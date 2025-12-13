package org.itmo.repository;

import org.itmo.model.Participation;
import org.itmo.model.enums.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByParticipantId(Long userId);
    List<Participation> findByEventId(Long eventId);
    Optional<Participation> findByParticipantIdAndEventId(Long userId, Long eventId);

    // --- НОВЫЙ МЕТОД: Подсчет подтвержденных участников ---
    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    // --- ИСПРАВЛЕНО: Используем JPQL с JOIN FETCH вместо @EntityGraph ---
    @Query("SELECT p FROM Participation p JOIN FETCH p.participant JOIN FETCH p.event WHERE p.participant.id = :userId AND p.status = :status")
    List<Participation> findByParticipantIdAndStatus(@Param("userId") Long userId, @Param("status") ParticipationStatus status);

    @Query("SELECT p FROM Participation p JOIN FETCH p.participant JOIN FETCH p.event WHERE p.event.organizer.id = :organizerId AND p.status = :status")
    List<Participation> findByEventOrganizerIdAndStatus(@Param("organizerId") Long organizerId, @Param("status") ParticipationStatus status);
    // --- КОНЕЦ ИСПРАВЛЕНИЯ ---
}
