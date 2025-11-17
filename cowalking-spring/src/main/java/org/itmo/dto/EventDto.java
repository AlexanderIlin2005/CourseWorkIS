
package org.itmo.dto;

import org.itmo.model.Event;
import org.itmo.model.enums.EventStatus; 
import lombok.Data;

import java.time.ZonedDateTime; 

@Data
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private Long organizerId; 
    private Long locationId;  
    private ZonedDateTime startTime; 
    private ZonedDateTime endTime;   
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status; 

    public EventDto() {}

    
    public EventDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.organizerId = event.getOrganizer().getId();
        this.locationId = event.getLocation().getId();
        this.startTime = event.getStartTime(); 
        this.endTime = event.getEndTime();     
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getCurrentParticipants();
        this.status = event.getStatus(); 
    }
}