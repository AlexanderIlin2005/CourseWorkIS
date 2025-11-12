package org.itmo.repository;

import org.itmo.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByParticipantId(Long userId);
    List<Participation> findByEventId(Long eventId);
    Optional<Participation> findByParticipantIdAndEventId(Long userId, Long eventId);
}
