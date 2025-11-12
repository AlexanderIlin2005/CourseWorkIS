package org.itmo.mapper;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class}) // Добавлены зависимости для мапперов связанных сущностей
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId") // Маппинг ID организатора
    @Mapping(source = "location.id", target = "locationId")   // Маппинг ID локации
    EventDto toEventDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent") // Уникальное имя
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent") // Уникальное имя
    @Mapping(target = "createdAt", ignore = true) // Игнорирование полей, устанавливаемых вручную
    @Mapping(target = "updatedAt", ignore = true)
    Event toEvent(EventDto eventDto);

    // Уникальные имена для избежания конфликта
    @Named("mapUserByIdForEvent")
    default org.itmo.model.User mapUserByIdForEvent(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        // В реальности здесь должен быть вызов сервиса для получения полного объекта
        return user;
    }

    @Named("mapLocationByIdForEvent")
    default org.itmo.model.Location mapLocationByIdForEvent(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.Location location = new org.itmo.model.Location();
        location.setId(id);
        // В реальности здесь должен быть вызов сервиса для получения полного объекта
        return location;
    }
}