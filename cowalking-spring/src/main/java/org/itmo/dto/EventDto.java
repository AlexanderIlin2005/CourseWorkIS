package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; 
import lombok.Data;

import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;

import org.itmo.model.enums.EventDifficulty; 


@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private String organizerUsername; 
    
    
    
    
    private LocalDateTime startTime; 
    private LocalDateTime endTime;   
    
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;

    private Long eventTypeId; 
    private String eventTypeName; 
    private EventDifficulty difficulty; 

    
    private Long durationMinutes;
    

    private Double averageRating;

    
    private Long reviewCount;
    

    
    private String photoUrl;
    

    private String organizerPhotoUrl; 

    
    private String startAddress;
    private String endAddress;
    

    
    private Double startLatitude;
    private Double startLongitude;
    


    public EventDto() {}

    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        
        if (event.getOrganizer() != null) {
            this.organizerId = event.getOrganizer().getId();
            this.organizerUsername = event.getOrganizer().getUsername(); 
            this.organizerPhotoUrl = event.getOrganizer().getPhotoUrl(); 
        }
        
        
        
        
        
        
        
        
        
        this.startTime = event.getStartTime(); 
        this.endTime = event.getEndTime();     
        
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus(); 

        
        if (event.getEventType() != null) {
            this.eventTypeId = event.getEventType().getId();
            this.eventTypeName = event.getEventType().getName();
        }
        this.difficulty = event.getDifficulty();
        

        
        if (event.getStartTime() != null && event.getEndTime() != null) {
            this.durationMinutes = java.time.Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
        }
        

        this.averageRating = event.getAverageRating();

        this.photoUrl = event.getPhotoUrl();

        
        this.startAddress = event.getStartAddress();
        this.endAddress = event.getEndAddress();
        

        
        this.startLatitude = event.getStartLatitude();
        this.startLongitude = event.getStartLongitude();
        
    }
}
