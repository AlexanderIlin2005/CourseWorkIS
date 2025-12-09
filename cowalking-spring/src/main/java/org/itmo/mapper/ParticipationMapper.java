// src/main/java/org/itmo/mapper/ParticipationMapper.java
package org.itmo.mapper;

import org.itmo.dto.ParticipationDto;
import org.itmo.model.Participation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface ParticipationMapper {

    @Mapping(source = "participant.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "joinedAt", target = "joinedAt") // joinedAt (LocalDateTime) маппится автоматически, но укажем явно для ясности
    @Mapping(source = "status", target = "status")     // status (ParticipationStatus enum) маппится автоматически
    ParticipationDto toParticipationDto(Participation participation);

    @Mapping(source = "userId", target = "participant", qualifiedByName = "mapUserByIdForParticipation")
    @Mapping(source = "eventId", target = "event", qualifiedByName = "mapEventByIdForParticipation")
    // joinedAt (LocalDateTime) маппится автоматически
    // status (ParticipationStatus enum) маппится автоматически
    @Mapping(target = "id", ignore = true) // id устанавливается при сохранении
        // УБРАНО: @Mapping(target = "createdAt", ignore = true) // <-- Нет такого поля в Participation
        // УБРАНО: @Mapping(target = "updatedAt", ignore = true) // <-- Нет такого поля в Participation
    Participation toParticipation(ParticipationDto participationDto);

    // --- ИМЕНОВАННЫЕ МЕТОДЫ для маппинга связей ---
    @Named("mapUserByIdForParticipation")
    default org.itmo.model.User mapUserByIdForParticipation(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        // В реальности здесь должен быть вызов сервиса для получения полного объекта
        return user;
    }

    @Named("mapEventByIdForParticipation")
    default org.itmo.model.Event mapEventByIdForParticipation(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.Event event = new org.itmo.model.Event();
        event.setId(id);
        // В реальности здесь должен быть вызов сервиса для получения полного объекта
        return event;
    }
    // --- КОНЕЦ ИМЕНОВАННЫХ МЕТОДОВ ---
}