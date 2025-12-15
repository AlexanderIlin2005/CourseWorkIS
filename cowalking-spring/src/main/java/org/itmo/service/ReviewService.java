
package org.itmo.service;

import org.itmo.model.Event;
import org.itmo.model.Review;
import org.itmo.dto.ReviewDto;
import org.itmo.model.User;
import org.itmo.repository.EventRepository;
import org.itmo.repository.ParticipationRepository;
import org.itmo.repository.ReviewRepository;
import org.itmo.util.MoscowTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.itmo.model.enums.ParticipationStatus; 

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    
    private boolean canUserReviewEvent(Event event, User user) {
        
        if (event.getOrganizer().getId().equals(user.getId())) {
            return false;
        }

        
        boolean isParticipant = participationRepository
                .findByParticipantIdAndEventId(user.getId(), event.getId())
                .filter(p -> p.getStatus() == org.itmo.model.enums.ParticipationStatus.CONFIRMED)
                .isPresent();

        if (!isParticipant) {
            return false;
        }

        
        LocalDateTime now = MoscowTimeUtil.getCurrentMoscowTime();
        if (event.getStartTime().isAfter(now)) {
            
            return false;
        }

        
        return true;
    }

    @Transactional
    public Review createOrUpdateReview(Long eventId, ReviewDto reviewDto, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!canUserReviewEvent(event, currentUser)) {
            throw new SecurityException("You are not allowed to review this event");
        }

        
        Optional<Review> existingReview = reviewRepository.findByEventIdAndUserId(eventId, currentUser.getId());

        Review review;
        if (existingReview.isPresent()) {
            
            review = existingReview.get();
            review.setRating(reviewDto.getRating());
            review.setComment(reviewDto.getComment());
        } else {
            
            review = new Review();
            review.setEvent(event);
            review.setUser(currentUser);
            review.setRating(reviewDto.getRating());
            review.setComment(reviewDto.getComment());
        }

        Review savedReview = reviewRepository.save(review);
        
        recalculateAverageRating(event);
        return savedReview;
    }

    @Transactional
    public void recalculateAverageRating(Event event) {
        List<Review> reviews = reviewRepository.findByEventId(event.getId());
        if (reviews.isEmpty()) {
            event.setAverageRating(null);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            event.setAverageRating(average);
        }
        eventRepository.save(event);
    }

    public List<Review> getReviewsForEvent(Long eventId) {
        return reviewRepository.findByEventId(eventId);
    }
}