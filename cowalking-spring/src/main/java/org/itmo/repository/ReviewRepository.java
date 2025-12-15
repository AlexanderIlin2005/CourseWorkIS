
package org.itmo.repository;

import org.itmo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEventId(Long eventId);
    Optional<Review> findByEventIdAndUserId(Long eventId, Long userId);
    Long countByEventId(Long eventId);
}