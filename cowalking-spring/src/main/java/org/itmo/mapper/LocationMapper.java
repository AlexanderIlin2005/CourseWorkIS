package org.itmo.mapper;

import org.itmo.dto.LocationDto;
import org.itmo.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toLocationDto(Location location);

    @Mapping(target = "createdAt", ignore = true) // Игнорируем createdAt при создании сущности из DTO
    @Mapping(target = "updatedAt", ignore = true) // Игнорируем updatedAt при создании сущности из DTO
    Location toLocation(LocationDto locationDto);
}
