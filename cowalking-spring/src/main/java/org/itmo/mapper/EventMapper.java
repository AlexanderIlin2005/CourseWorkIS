// src/main/java/org/itmo/mapper/EventMapper.java
package org.itmo.mapper;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.itmo.model.Location; // Импортируем Location

@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "organizer.username", target = "organizerUsername") // <-- Маппинг имени организатора при чтении (Event -> EventDto)
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "location.name", target = "locationName") // <-- Маппинг имени локации при чтении (Event -> EventDto)
    @Mapping(source = "location.address", target = "locationAddress") // <-- Маппинг адреса локации при чтении (Event -> EventDto)
    @Mapping(source = "startTime", target = "startTime", ignore = true) // <-- Игнорируем при маппинге Event -> EventDto (преобразование в строку вручную)
    @Mapping(source = "endTime", target = "endTime", ignore = true)     // <-- Игнорируем при маппинге Event -> EventDto (преобразование в строку вручную)
    EventDto toEventDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent")
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent")
    @Mapping(target = "startTime", ignore = true) // <-- Игнорируем startTime при маппинге EventDto -> Event (устанавливается в контроллере)
    @Mapping(target = "endTime", ignore = true)   // <-- Игнорируем endTime при маппинге EventDto -> Event (устанавливается в контроллере)
    // --- УБРАНО: @Mapping(target = "locationName", ignore = true) ---
    // --- УБРАНО: @Mapping(target = "locationAddress", ignore = true) ---
    // --- УБРАНО: @Mapping(target = "organizerUsername", ignore = true) ---
    // Эти поля НЕ существуют в Event, их нельзя устанавливать через MapStruct при маппинге DTO -> Entity.
    @Mapping(target = "createdAt", ignore = true) // Игнорирование полей, устанавливаемых вручную
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true) // Игнорируем status при создании/редактировании через DTO
    @Mapping(target = "currentParticipants", ignore = true) // Игнорируем currentParticipants при маппинге DTO -> Entity
    Event toEvent(EventDto eventDto);

    // --- ИМЕНОВАННЫЕ МЕТОДЫ для маппинга связей ---
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
    // --- КОНЕЦ ИМЕНОВАННЫХ МЕТОДОВ ---
}