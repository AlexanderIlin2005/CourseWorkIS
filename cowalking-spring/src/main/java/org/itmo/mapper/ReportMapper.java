package org.itmo.mapper;

import org.itmo.dto.ReportDto;
import org.itmo.model.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class}) // Добавлены зависимости для мапперов связанных сущностей
public interface ReportMapper {

    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reportedUser.id", target = "reportedUserId")
    @Mapping(source = "event.id", target = "eventId")
    ReportDto toReportDto(Report report);

    @Mapping(source = "reporterId", target = "reporter", qualifiedByName = "mapUserByIdForReport") // Уникальное имя
    @Mapping(source = "reportedUserId", target = "reportedUser", qualifiedByName = "mapUserByIdForReport") // Уникальное имя
    @Mapping(source = "eventId", target = "event", qualifiedByName = "mapEventByIdForReport") // Уникальное имя
    Report toReport(ReportDto reportDto);

    // Уникальные имена для избежания конфликта
    @Named("mapUserByIdForReport")
    default org.itmo.model.User mapUserByIdForReport(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        return user;
    }

    @Named("mapEventByIdForReport")
    default org.itmo.model.Event mapEventByIdForReport(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.Event event = new org.itmo.model.Event();
        event.setId(id);
        return event;
    }
}