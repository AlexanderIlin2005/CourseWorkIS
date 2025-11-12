package org.itmo.mapper;

import org.itmo.dto.ReportDto;
import org.itmo.model.Report;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportDto toReportDto(Report report);
    Report toReport(ReportDto reportDto);
}
