package org.itmo.service;

import org.itmo.model.Event;
import org.itmo.model.Participation;
import org.itmo.model.User;
import org.itmo.repository.EventRepository;
import org.itmo.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;

    public List<Participation> findByParticipantId(Long userId) {
        return participationRepository.findByParticipantId(userId);
    }

    public List<Participation> findByEventId(Long eventId) {
        return participationRepository.findByEventId(eventId);
    }

    @Transactional
    public Participation joinEvent(Long eventId, User user) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        
        if (event.getMaxParticipants() != null &&
            event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        
        Optional<Participation> existingParticipation =
            participationRepository.findByParticipantIdAndEventId(user.getId(), eventId);
        if (existingParticipation.isPresent()) {
            throw new RuntimeException("User already joined this event");
        }

        Participation participation = new Participation();
        participation.setParticipant(user);
        participation.setEvent(event);
        participation.setStatus(Participation.ParticipationStatus.CONFIRMED);

        Participation saved = participationRepository.save(participation);

        
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        return saved;
    }

    @Transactional
    public void leaveEvent(Long eventId, User user) {
        Participation participation = participationRepository
            .findByParticipantIdAndEventId(user.getId(), eventId)
            .orElseThrow(() -> new RuntimeException("User not joined this event"));

        participationRepository.delete(participation);

        
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
        eventRepository.save(event);
    }
}
