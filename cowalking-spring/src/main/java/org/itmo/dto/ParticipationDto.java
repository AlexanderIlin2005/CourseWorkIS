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

    // --- ДОБАВЛЕНО: Информация об участнике и событии для отображения ---
    private String participantUsername; // Имя пользователя-участника
    private String eventTitle;          // Название события
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    private LocalDateTime joinedAt;
    private ParticipationStatus status;

    public ParticipationDto() {}

    public ParticipationDto(Participation participation) {
        this.id = participation.getId();
        this.userId = participation.getParticipant().getId();
        this.eventId = participation.getEvent().getId();
        // --- ДОБАВЛЕНО: Заполняем информацию для отображения ---
        this.participantUsername = participation.getParticipant().getUsername();
        this.eventTitle = participation.getEvent().getTitle();
        // --- КОНЕЦ ДОБАВЛЕНИЯ ---
        this.joinedAt = participation.getJoinedAt();
        this.status = participation.getStatus();
    }
}
