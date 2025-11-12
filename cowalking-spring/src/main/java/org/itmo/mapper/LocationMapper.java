package org.itmo.mapper;

import org.itmo.dto.LocationDto;
import org.itmo.model.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toLocationDto(Location location);
    Location toLocation(LocationDto locationDto);
}
