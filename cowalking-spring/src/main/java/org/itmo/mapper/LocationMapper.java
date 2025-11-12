package org.itmo.mapper;

import org.itmo.dto.LocationDto;
import org.itmo.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toLocationDto(Location location);

    @Mapping(target = "createdAt", ignore = true) // Игнорирование полей, устанавливаемых вручную
    @Mapping(target = "updatedAt", ignore = true)
    Location toLocation(LocationDto locationDto);
}