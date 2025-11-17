package org.itmo.mapper;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class}) 
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId") 
    @Mapping(source = "location.id", target = "locationId")   
    EventDto toEventDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent") 
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent") 
    @Mapping(target = "createdAt", ignore = true) 
    @Mapping(target = "updatedAt", ignore = true)
    Event toEvent(EventDto eventDto);

    
    @Named("mapUserByIdForEvent")
    default org.itmo.model.User mapUserByIdForEvent(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        
        return user;
    }

    @Named("mapLocationByIdForEvent")
    default org.itmo.model.Location mapLocationByIdForEvent(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.Location location = new org.itmo.model.Location();
        location.setId(id);
        
        return location;
    }
}