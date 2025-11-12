package org.itmo.service;

import org.itmo.model.Report;
import org.itmo.model.User;
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

    @Transactional
    public Report createReport(Report report, User reporter) {
        report.setReporter(reporter);
        report.setCreatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public Report resolveReport(Long reportId, String resolutionNotes, User resolver) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setResolvedAt(LocalDateTime.now());
        report.setIsResolved(true);
        report.setResolutionNotes(resolutionNotes);

        return reportRepository.save(report);
    }
}
