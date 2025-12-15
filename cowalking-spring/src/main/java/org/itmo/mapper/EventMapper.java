package org.itmo.mapper;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import java.time.Duration;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "organizer.username", target = "organizerUsername") 
    @Mapping(source = "organizer.photoUrl", target = "organizerPhotoUrl") 
    
    
    
    
    
    @Mapping(source = "eventType.id", target = "eventTypeId")
    @Mapping(source = "eventType.name", target = "eventTypeName")
    
    @Mapping(source = "startAddress", target = "startAddress")
    @Mapping(source = "endAddress", target = "endAddress")
        
    
    @Mapping(source = "startLatitude", target = "startLatitude")
    @Mapping(source = "startLongitude", target = "startLongitude")
    EventDto toEventDto(Event event);


    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent")
    
    
    
    @Mapping(source = "photoUrl", target = "photoUrl")
    
    @Mapping(target = "createdAt", ignore = true) 
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true) 
    @Mapping(target = "currentParticipants", ignore = true) 
    Event toEvent(EventDto eventDto);

    
    @AfterMapping
    default void calculateDuration(@MappingTarget EventDto dto, Event event) {
        if (event.getStartTime() != null && event.getEndTime() != null) {
            dto.setDurationMinutes(Duration.between(event.getStartTime(), event.getEndTime()).toMinutes());
        }
        
    }

    
    @Named("mapUserByIdForEvent")
    default org.itmo.model.User mapUserByIdForEvent(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        
        return user;
    }

    
    
    
    
    
    
    
    
    
    
    

}
