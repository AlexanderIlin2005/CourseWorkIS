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

    // --- ИЗМЕНЕНО: маппинг organizer.username в organizerUsername ---
    @Mapping(source = "organizer.username", target = "organizerUsername") // <-- Маппим имя организатора
    @Mapping(source = "organizer.id", target = "organizerId")             // <-- Маппим ID организатора
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    // --- ИЗМЕНЕНО: маппинг location.name и location.address ---
    @Mapping(source = "location.name", target = "locationName")      // <-- Маппим имя локации
    @Mapping(source = "location.address", target = "locationAddress") // <-- Маппим адрес локации
    @Mapping(source = "location.id", target = "locationId")          // <-- Маппим ID локации
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    EventDto toEventDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent")
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent")
    @Mapping(target = "createdAt", ignore = true) // Игнорируем поля, устанавливаемые вручную
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true) // Игнорируем статус при создании/редактировании через DTO
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