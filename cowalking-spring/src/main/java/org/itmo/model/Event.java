package org.itmo.model;

import org.itmo.model.converters.LocalDateTimeConverter; 
import org.itmo.model.enums.EventStatus; 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; 

import org.itmo.model.enums.EventDifficulty; 

@Entity
@Table(name = "cowalking_events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    
    
    
    
    

    
    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; 

    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; 
    

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Convert(converter = LocalDateTimeConverter.class) 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.ACTIVE; 
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id")
    private EventType eventType; 

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private EventDifficulty difficulty; 

    @Column(name = "average_rating")
    private Double averageRating; 

    
    @Column(length = 500)
    private String photoUrl;
    

    
    @Column(name = "start_address", length = 500, nullable = false)
    private String startAddress;

    @Column(name = "end_address", length = 500)
    private String endAddress;
    

    
    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;
    

}
