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

    // --- ИЗМЕНЕНО: маппинг location.name в locationName ---
    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "location.name", target = "locationName") // <-- Маппим имя локации
    @Mapping(source = "location.address", target = "locationAddress") // <-- Добавить
    @Mapping(source = "location.id", target = "locationId")     // <-- Маппим ID локации
    EventDto toEventDto(Event event);
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent")
    // --- ИЗМЕНЕНО: маппинг locationId в location ---
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent") // <-- Маппим ID локации в объект
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---
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