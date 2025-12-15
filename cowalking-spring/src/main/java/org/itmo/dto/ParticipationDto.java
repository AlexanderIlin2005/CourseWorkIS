package org.itmo.dto;

import org.itmo.model.Participation;
import org.itmo.model.enums.ParticipationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationDto {
    private Long id;
    private Long userId;
    private Long eventId;

    
    private String participantUsername; 
    private String eventTitle;          
    

    private LocalDateTime joinedAt;
    private ParticipationStatus status;

    public ParticipationDto() {}

    public ParticipationDto(Participation participation) {
        this.id = participation.getId();
        this.userId = participation.getParticipant().getId();
        this.eventId = participation.getEvent().getId();
        
        this.participantUsername = participation.getParticipant().getUsername();
        this.eventTitle = participation.getEvent().getTitle();
        
        this.joinedAt = participation.getJoinedAt();
        this.status = participation.getStatus();
    }
}
