package org.itmo.service;

import org.itmo.model.Report;
import org.itmo.model.User;
import org.itmo.model.enums.UserRole;
import org.itmo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    public List<Report> findByIsResolved(Boolean isResolved) {
        return reportRepository.findByIsResolved(isResolved);
    }

    public List<Report> findByReportedUserId(Long reportedUserId) {
        return reportRepository.findByReportedUserId(reportedUserId);
    }

    public List<Report> findByEventId(Long eventId) {
        return reportRepository.findByEventId(eventId);
    }

    @Transactional
    public Report createReport(Report report, User reporter) {
        report.setReporter(reporter);
        report.setCreatedAt(LocalDateTime.now());
        report.setIsResolved(false); // Новый отчет не решен
        return reportRepository.save(report);
    }

    @Transactional
    public Report resolveReport(Long reportId, String resolutionNotes, User resolver) {
        // Проверка прав: только админ может решать отчеты
        if (!resolver.getRole().equals(UserRole.ADMIN)) {
            throw new SecurityException("Only administrators can resolve reports");
        }

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setResolvedAt(LocalDateTime.now());
        report.setIsResolved(true);
        report.setResolutionNotes(resolutionNotes);

        return reportRepository.save(report);
    }
}
