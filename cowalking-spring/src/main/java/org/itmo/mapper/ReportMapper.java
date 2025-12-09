package org.itmo.mapper;

import org.itmo.dto.ReportDto;
import org.itmo.model.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface ReportMapper {

    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reportedUser.id", target = "reportedUserId")
    @Mapping(source = "event.id", target = "eventId")
    ReportDto toReportDto(Report report);

    @Mapping(source = "reporterId", target = "reporter", qualifiedByName = "mapUserByIdForReport")
    @Mapping(source = "reportedUserId", target = "reportedUser", qualifiedByName = "mapUserByIdForReport")
    @Mapping(source = "eventId", target = "event", qualifiedByName = "mapEventByIdForReport")
    @Mapping(target = "createdAt", ignore = true) // Игнорируем createdAt при создании сущности из DTO
    @Mapping(target = "resolvedAt", ignore = true) // Игнорируем resolvedAt при создании сущности из DTO
    @Mapping(target = "isResolved", ignore = true) // Игнорируем isResolved при создании сущности из DTO
    Report toReport(ReportDto reportDto);

    // --- ИМЕНОВАННЫЕ МЕТОДЫ для маппинга связей ---
    @Named("mapUserByIdForReport")
    default org.itmo.model.User mapUserByIdForReport(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.User user = new org.itmo.model.User();
        user.setId(id);
        // В реальности здесь должен быть вызов сервиса для получения полного объекта
        return user;
    }

    @Named("mapEventByIdForReport")
    default org.itmo.model.Event mapEventByIdForReport(Long id) {
        if (id == null) {
            return null;
        }
        org.itmo.model.Event event = new org.itmo.model.Event();
        event.setId(id);
        // В реальности здесь должен быть вызов сервиса для получения полного объекта
        return event;
    }
    // --- КОНЕЦ ИМЕНОВАННЫХ МЕТОДОВ ---
}
