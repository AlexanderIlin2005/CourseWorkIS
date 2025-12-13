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

        // Проверка: не достигнут ли максимум участников (только для подтвержденных)
        long confirmedParticipants = participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
        if (event.getMaxParticipants() != null && confirmedParticipants >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        // Проверка: не отправлял ли уже пользователь заявку
        Optional<Participation> existingParticipation = participationRepository
                .findByParticipantIdAndEventId(user.getId(), eventId);
        if (existingParticipation.isPresent()) {
            throw new RuntimeException("You have already applied to join this event");
        }

        Participation participation = new Participation();
        participation.setParticipant(user);
        participation.setEvent(event);
        participation.setStatus(ParticipationStatus.PENDING); // <-- Новый статус PENDING
        participation.setJoinedAt(LocalDateTime.now());

        return participationRepository.save(participation);
        // ЗАМЕЧАНИЕ: currentParticipants НЕ увеличивается здесь
    }

    @Transactional
    public void leaveEvent(Long eventId, User user) {
        Participation participation = participationRepository
                .findByParticipantIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new RuntimeException("You are not joined to this event"));

        // Удаляем заявку или отменяем участие
        participationRepository.delete(participation);

        // Если статус был CONFIRMED, уменьшаем currentParticipants
        if (ParticipationStatus.CONFIRMED.equals(participation.getStatus())) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
            eventRepository.save(event);
        }
    }

    // --- НОВЫЙ МЕТОД: Подтверждение заявки организатором ---
    @Transactional
    public void confirmParticipation(Long participationId, User organizer) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Проверка: является ли пользователь организатором события
        if (!participation.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new SecurityException("You can only confirm applications for your own events");
        }

        // Проверка: статус заявки должен быть PENDING
        if (!ParticipationStatus.PENDING.equals(participation.getStatus())) {
            throw new IllegalStateException("Application is not pending");
        }

        // Проверка: не достигнут ли максимум участников
        Event event = participation.getEvent();
        long confirmedParticipants = participationRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.CONFIRMED);
        if (event.getMaxParticipants() != null && confirmedParticipants >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full, cannot confirm this application");
        }

        // Подтверждаем заявку
        participation.setStatus(ParticipationStatus.CONFIRMED);
        participationRepository.save(participation);

        // Увеличиваем currentParticipants
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);
    }

    // --- НОВЫЙ МЕТОД: Отклонение заявки организатором ---
    @Transactional
    public void rejectParticipation(Long participationId, User organizer) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Проверка: является ли пользователь организатором события
        if (!participation.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new SecurityException("You can only reject applications for your own events");
        }

        // Проверка: статус заявки должен быть PENDING
        if (!ParticipationStatus.PENDING.equals(participation.getStatus())) {
            throw new IllegalStateException("Application is not pending");
        }

        // Отклоняем заявку
        participation.setStatus(ParticipationStatus.CANCELLED);
        participationRepository.save(participation);
        // currentParticipants НЕ меняется
    }



}
