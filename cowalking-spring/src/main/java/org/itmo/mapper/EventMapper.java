// src/main/java/org/itmo/mapper/EventMapper.java
package org.itmo.mapper;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId") // <-- Маппинг ID организатора
    @Mapping(source = "organizer.username", target = "organizerUsername") // <-- Маппинг имени организатора в DTO
    @Mapping(source = "location.id", target = "locationId")   // <-- Маппинг ID локации
    @Mapping(source = "location.name", target = "locationName") // <-- Маппинг имени локации в DTO
    @Mapping(source = "location.address", target = "locationAddress") // <-- Маппинг адреса локации в DTO
        // startTime и endTime (String в DTO) будут маппиться в LocalDateTime в Event через конвертеры, если они определены
        // Или если в Event startTime/endTime String, то маппятся напрямую
        // Или если в Event LocalDateTime, и в DTO LocalDateTime, то маппятся напрямую
        // Или, если в Event LocalDateTime, и в DTO String, то нужен @Named метод или @Converter для преобразования
        // Сейчас, после ваших изменений, в Event LocalDateTime, в DTO String.
        // MapStruct не может сам преобразовать String <-> LocalDateTime без указания способа.
        // Поэтому вручную в контроллере используем eventDto.getParsedStartTime/EndTime().
        // Убираем startTime и endTime из маппинга @Mapping.
        // startTime и endTime будут установлены вручную в контроллере
    EventDto toEventDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent") // <-- Маппинг объекта организатора по ID
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent") // <-- Маппинг объекта локации по ID
    // startTime и endTime (String в DTO) будут маппиться в LocalDateTime в Event через конвертеры, если они определены
    // Или если в Event startTime/endTime String, то маппятся напрямую
    // Или если в Event LocalDateTime, и в DTO LocalDateTime, то маппятся напрямую
    // Или, если в Event LocalDateTime, и в DTO String, то нужен @Named метод или @Converter для преобразования
    // Сейчас, после ваших изменений, в Event LocalDateTime, в DTO String.
    // MapStruct не может сам преобразовать String <-> LocalDateTime без указания способа.
    // Поэтому вручную в контроллере используем eventDto.getParsedStartTime/EndTime().
    // Убираем startTime и endTime из маппинга @Mapping.
    // startTime и endTime будут установлены вручную в контроллере
    // --- УБРАНО: @Mapping(target = "startTime", ignore = true) ---
    // --- УБРАНО: @Mapping(target = "endTime", ignore = true) ---
    // --- УБРАНО: @Mapping(target = "locationName", ignore = true) ---
    // --- УБРАНО: @Mapping(target = "locationAddress", ignore = true) ---
    // --- УБРАНО: @Mapping(target = "organizerUsername", ignore = true) ---
    // --- КОНЕЦ УБРАНОГО ---
    @Mapping(target = "createdAt", ignore = true) // <-- Игнорировать createdAt при маппинге EventDto -> Event
    @Mapping(target = "updatedAt", ignore = true) // <-- Игнорировать updatedAt при маппинге EventDto -> Event
    @Mapping(target = "status", ignore = true) // <-- Игнорировать status при маппинге EventDto -> Event
    @Mapping(target = "currentParticipants", ignore = true) // <-- Игнорировать currentParticipants при маппинге EventDto -> Event
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