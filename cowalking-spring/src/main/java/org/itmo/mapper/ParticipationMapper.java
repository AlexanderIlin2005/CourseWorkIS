package org.itmo.mapper;

import org.itmo.dto.ParticipationDto;
import org.itmo.model.Participation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class}) // Добавлены зависимости для мапперов связанных сущностей
public interface ParticipationMapper {

    @Mapping(source = "participant.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    ParticipationDto toParticipationDto(Participation participation);

    @Mapping(source = "userId", target = "participant", qualifiedByName = "mapUserByIdForParticipation") // Уникальное имя
    @Mapping(source = "eventId", target = "event", qualifiedByName = "mapEventByIdForParticipation") // Уникальное имя
    Participation toParticipation(ParticipationDto participationDto);

    // Уникальные имена для избежания конфликта
    @Named("mapUserByIdForParticipation")
    default org.itmo.model.User mapUserByIdForParticipation(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        return user;
    }

    @Named("mapEventByIdForParticipation")
    default org.itmo.model.Event mapEventByIdForParticipation(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.Event event = new org.itmo.model.Event();
        event.setId(id);
        return event;
    }
}