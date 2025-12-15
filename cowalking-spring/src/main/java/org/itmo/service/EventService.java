
package org.itmo.service;

import org.itmo.model.Event;
import org.itmo.model.User;
import org.itmo.model.enums.EventStatus;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.itmo.repository.ReviewRepository; 

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.stream.Collectors; 

import org.itmo.util.MoscowTimeUtil; 

import org.itmo.model.Event;
import org.itmo.model.enums.EventDifficulty;






@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final ReviewRepository reviewRepository; 

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findByOrganizerId(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    
    
    

    @Transactional
    public Event save(Event event, User currentUser) {
        
        if (event.getStartTime() != null && event.getEndTime() != null && event.getStartTime().isAfter(event.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        
        LocalDateTime currentMoscowTime = MoscowTimeUtil.getCurrentMoscowTime();
        if (event.getStartTime() != null && event.getStartTime().isBefore(currentMoscowTime.minusMinutes(1))) {
            throw new IllegalArgumentException("Start time cannot be in the past.");
        }

        if (event.getId() == null) {
            event.setOrganizer(currentUser);
            
            if (event.getEndTime() != null && !event.getEndTime().isAfter(event.getStartTime())) {
                throw new IllegalArgumentException("End time must be after start time.");
            }
            event.setStatus(EventStatus.ACTIVE);
        } else {
            
            
            Event existingEvent = eventRepository.findById(event.getId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            if (EventStatus.COMPLETED.equals(existingEvent.getStatus())) {
                
                
                throw new SecurityException("Cannot edit a completed event.");
            }
            
            if (event.getEndTime() != null && !event.getEndTime().isAfter(event.getStartTime())) {
                throw new IllegalArgumentException("End time must be after start time.");
            }
            
            event.setOrganizer(existingEvent.getOrganizer());
        }

        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteById(Long id, User currentUser) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        
        if (EventStatus.COMPLETED.equals(event.getStatus())) {
            throw new SecurityException("Cannot delete a completed event.");
        }
        if (!event.getOrganizer().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new SecurityException("You can only delete active events you organized.");
        }

        eventRepository.deleteById(id);
    }

    public List<Event> findAllSorted(String sort, String direction) {
        
        if (!"startTime".equals(sort) && !"endTime".equals(sort)) {
            sort = "startTime"; 
        }
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            direction = "asc"; 
        }

        
        Sort sortDirection = "asc".equals(direction) ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        return eventRepository.findAll(sortDirection);
    }

    
    public List<Event> findActiveEvents() {
        List<Event> events = eventRepository.findEventsWithAllDetailsByStatus(EventStatus.ACTIVE);
        events.forEach(this::updateEventStatusIfNeeded);
        
        return events.stream()
                .filter(e -> EventStatus.ACTIVE.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    
    public List<Event> findCompletedEvents() {
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .filter(event -> {
                    
                    updateEventStatusIfNeeded(event);
                    
                    return EventStatus.COMPLETED.equals(event.getStatus());
                })
                .collect(Collectors.toList());
    }

    
    public void updateEventStatusIfNeeded(Event event) {
        if (EventStatus.ACTIVE.equals(event.getStatus()) &&
                MoscowTimeUtil.isEventCompleted(event.getEndTime())) {
            event.setStatus(EventStatus.COMPLETED);
            
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event); 
        }
    }

    
    public long getReviewCountForEvent(Long eventId) {
        return reviewRepository.countByEventId(eventId);
    }


    
    /**
     * Находит активные события с фильтрацией по типу, сложности и продолжительности.
     * Фильтрация происходит на стороне Java после получения всех активных событий.
     */
    public List<Event> findActiveEventsFiltered(
            Long eventTypeId,
            EventDifficulty difficulty,
            Integer minDurationMinutes,
            Integer maxDurationMinutes) {

        List<Event> events;
        
        if (eventTypeId != null && difficulty != null) {
            events = eventRepository.findEventsWithAllDetailsByStatusEventTypeAndDifficulty(
                    EventStatus.ACTIVE, eventTypeId, difficulty);
        } else if (eventTypeId != null) {
            events = eventRepository.findEventsWithAllDetailsByStatusAndEventType(
                    EventStatus.ACTIVE, eventTypeId);
        } else if (difficulty != null) {
            events = eventRepository.findEventsWithAllDetailsByStatusAndDifficulty(
                    EventStatus.ACTIVE, difficulty);
        } else {
            
            events = eventRepository.findEventsWithAllDetailsByStatus(EventStatus.ACTIVE);
        }

        
        events.forEach(this::updateEventStatusIfNeeded);
        
        events = events.stream()
                .filter(e -> EventStatus.ACTIVE.equals(e.getStatus()))
                .collect(Collectors.toList());

        
        if (minDurationMinutes != null || maxDurationMinutes != null) {
            events = events.stream().filter(event -> {
                if (event.getStartTime() == null || event.getEndTime() == null) {
                    return false;
                }
                long durationMinutes = java.time.Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
                boolean minOk = (minDurationMinutes == null) || (durationMinutes >= minDurationMinutes);
                boolean maxOk = (maxDurationMinutes == null) || (durationMinutes <= maxDurationMinutes);
                return minOk && maxOk;
            }).collect(Collectors.toList());
        }

        return events;
    }
    


}