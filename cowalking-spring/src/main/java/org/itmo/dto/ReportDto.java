package org.itmo.dto;

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
}
