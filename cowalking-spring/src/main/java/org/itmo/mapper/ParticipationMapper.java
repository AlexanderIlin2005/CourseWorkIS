package org.itmo.mapper;

import org.itmo.dto.ParticipationDto;
import org.itmo.model.Participation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParticipationMapper {
    ParticipationDto toParticipationDto(Participation participation);
    Participation toParticipation(ParticipationDto participationDto);
}
