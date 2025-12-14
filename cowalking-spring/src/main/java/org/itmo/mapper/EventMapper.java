package org.itmo.mapper;

import org.itmo.dto.EventDto;
import org.itmo.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import java.time.Duration;

@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "organizer.username", target = "organizerUsername") // <-- Маппинг имени организатора
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "location.name", target = "locationName") // <-- Маппинг имени локации
    @Mapping(source = "location.address", target = "locationAddress") // <-- Маппинг адреса локации
    // startTime и endTime (LocalDateTime) маппятся автоматически
    // --- ДОБАВЛЕНО: Маппинг для EventType ---
    @Mapping(source = "eventType.id", target = "eventTypeId")
    @Mapping(source = "eventType.name", target = "eventTypeName")
    EventDto toEventDto(Event event);

    @Mapping(source = "organizerId", target = "organizer", qualifiedByName = "mapUserByIdForEvent")
    @Mapping(source = "locationId", target = "location", qualifiedByName = "mapLocationByIdForEvent")
    // startTime и endTime (LocalDateTime) маппятся автоматически
    // --- ДОБАВЛЕНО: Маппинг для фото ---
    @Mapping(source = "photoUrl", target = "photoUrl")
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
    @Mapping(target = "createdAt", ignore = true) // Игнорирование полей, устанавливаемых вручную
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true) // Игнорируем status при создании/редактировании через DTO
    @Mapping(target = "currentParticipants", ignore = true) // Игнорируем currentParticipants при создании/редактировании через DTO
    Event toEvent(EventDto eventDto);

    // --- ДОБАВЛЕНО: Метод для вычисления продолжительности ---
    @AfterMapping
    default void calculateDuration(@MappingTarget EventDto dto, Event event) {
        if (event.getStartTime() != null && event.getEndTime() != null) {
            dto.setDurationMinutes(Duration.between(event.getStartTime(), event.getEndTime()).toMinutes());
        }
        // Если startTime или endTime null, durationMinutes останется null (по умолчанию)
    }

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
