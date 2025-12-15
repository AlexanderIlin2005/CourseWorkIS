package org.itmo.service;

import org.itmo.model.Event;
import org.itmo.model.Participation;
import org.itmo.model.User;
import org.itmo.model.enums.ParticipationStatus;
import org.itmo.repository.EventRepository;
import org.itmo.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        
        long confirmedParticipants = participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
        if (event.getMaxParticipants() != null && confirmedParticipants >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        
        Optional<Participation> existingParticipation = participationRepository
                .findByParticipantIdAndEventId(user.getId(), eventId);

        
        if (existingParticipation.isPresent()) {
            Participation existing = existingParticipation.get();
            
            if (existing.getStatus() == ParticipationStatus.PENDING ||
                    existing.getStatus() == ParticipationStatus.CONFIRMED) {
                throw new RuntimeException("You have already applied to join this event");
            }
            
            participationRepository.delete(existing);
        }

        Participation participation = new Participation();
        participation.setParticipant(user);
        participation.setEvent(event);
        participation.setStatus(ParticipationStatus.PENDING);
        participation.setJoinedAt(LocalDateTime.now());

        return participationRepository.save(participation);
    }

    @Transactional
    public void leaveEvent(Long eventId, User user) {
        Participation participation = participationRepository
                .findByParticipantIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new RuntimeException("You are not joined to this event"));

        
        participationRepository.delete(participation);

        
        if (ParticipationStatus.CONFIRMED.equals(participation.getStatus())) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
            eventRepository.save(event);
        }
    }

    
    @Transactional
    public void confirmParticipation(Long participationId, User organizer) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        
        if (!participation.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new SecurityException("You can only confirm applications for your own events");
        }

        
        if (!ParticipationStatus.PENDING.equals(participation.getStatus())) {
            throw new IllegalStateException("Application is not pending");
        }

        
        Event event = participation.getEvent();
        long confirmedParticipants = participationRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.CONFIRMED);
        if (event.getMaxParticipants() != null && confirmedParticipants >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full, cannot confirm this application");
        }

        
        participation.setStatus(ParticipationStatus.CONFIRMED);
        participationRepository.save(participation);

        
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);
    }

    
    @Transactional
    public void rejectParticipation(Long participationId, User organizer) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        
        if (!participation.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new SecurityException("You can only reject applications for your own events");
        }

        
        if (!ParticipationStatus.PENDING.equals(participation.getStatus())) {
            throw new IllegalStateException("Application is not pending");
        }

        
        participation.setStatus(ParticipationStatus.CANCELLED);
        participationRepository.save(participation);
        
    }

    public Participation findByParticipantIdAndEventId(Long participantId, Long eventId) {
        return participationRepository.findByParticipantIdAndEventId(participantId, eventId)
                .orElse(null); 
    }



}
