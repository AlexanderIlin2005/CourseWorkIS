package org.itmo.dto;

import org.itmo.model.Report;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDto {
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private Long eventId;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Boolean isResolved;
    private String resolutionNotes;

    public ReportDto() {}

    public ReportDto(Report report) {
        this.id = report.getId();
        this.reporterId = report.getReporter().getId();
        this.reportedUserId = report.getReportedUser() != null ? report.getReportedUser().getId() : null;
        this.eventId = report.getEvent() != null ? report.getEvent().getId() : null;
        this.reason = report.getReason();
        this.createdAt = report.getCreatedAt();
        this.resolvedAt = report.getResolvedAt();
        this.isResolved = report.getIsResolved();
        this.resolutionNotes = report.getResolutionNotes();
    }
}
