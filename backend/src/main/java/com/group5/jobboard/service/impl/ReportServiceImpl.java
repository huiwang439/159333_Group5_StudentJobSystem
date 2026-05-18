package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.AnalyticsLog;
import com.group5.jobboard.entity.Report;
import com.group5.jobboard.repository.AnalyticsLogRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.ReportRepository;
import com.group5.jobboard.repository.UserRepository;
import com.group5.jobboard.service.NotificationService;
import com.group5.jobboard.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;
    private final AnalyticsLogRepository analyticsLogRepository;

    public ReportServiceImpl(ReportRepository reportRepository,
                             UserRepository userRepository,
                             JobRepository jobRepository,
                             NotificationService notificationService,
                             AnalyticsLogRepository analyticsLogRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
        this.analyticsLogRepository = analyticsLogRepository;
    }

    @Override
    public Map<String, Object> createReport(Long reporterUserId,
                                            Long reportedUserId,
                                            Long relatedJobId,
                                            String reason,
                                            String description) {
        userRepository.findById(reporterUserId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        if (reportedUserId != null) {
            userRepository.findById(reportedUserId)
                    .orElseThrow(() -> new RuntimeException("Reported user not found"));
        }

        if (relatedJobId != null) {
            jobRepository.findById(relatedJobId)
                    .orElseThrow(() -> new RuntimeException("Related job not found"));
        }

        Report report = new Report();
        report.setReporterUserId(reporterUserId);
        report.setReportedUserId(reportedUserId);
        report.setRelatedJobId(relatedJobId);
        report.setReportReason(reason);
        report.setReportDetails(description);
        report.setReportStatus("pending");

        Report saved = reportRepository.save(report);

        log(reporterUserId, "CREATE_REPORT", "REPORT", saved.getId());

        Map<String, Object> result = reportToMap(saved);
        result.put("created", true);

        return result;
    }

    @Override
    public List<Map<String, Object>> getMyReports(Long reporterUserId) {
        return reportRepository.findByReporterUserId(reporterUserId)
                .stream()
                .map(this::reportToMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getAllReports(String reportStatus) {
        List<Report> reports;

        if (reportStatus == null || reportStatus.isBlank()) {
            reports = reportRepository.findAll();
        } else {
            reports = reportRepository.findByReportStatus(reportStatus);
        }

        return reports.stream()
                .map(this::reportToMap)
                .toList();
    }

    @Override
    public Map<String, Object> handleReport(Long reportId, String reportStatus, Long operatorId) {
        if (!"resolved".equals(reportStatus)
                && !"rejected".equals(reportStatus)
                && !"pending".equals(reportStatus)) {
            throw new RuntimeException("Invalid report status");
        }

        userRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Handler not found"));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setReportStatus(reportStatus);
        report.setHandledBy(operatorId);
        report.setHandledAt(LocalDateTime.now());

        Report saved = reportRepository.save(report);

        log(operatorId, "HANDLE_REPORT", "REPORT", reportId);

        notificationService.createNotification(
                saved.getReporterUserId(),
                "REPORT_RESULT",
                "Report handled",
                "Your report has been handled. Status: " + reportStatus
        );

        if (saved.getReportedUserId() != null && "resolved".equals(reportStatus)) {
            notificationService.createNotification(
                    saved.getReportedUserId(),
                    "REPORT_WARNING",
                    "You have been reported",
                    "A report related to your account has been handled."
            );
        }

        Map<String, Object> result = reportToMap(saved);
        result.put("handled", true);

        return result;
    }

    private Map<String, Object> reportToMap(Report report) {
        Map<String, Object> item = new HashMap<>();

        item.put("reportId", report.getId());
        item.put("reporterUserId", report.getReporterUserId());
        item.put("reportedUserId", report.getReportedUserId());
        item.put("relatedJobId", report.getRelatedJobId());
        item.put("reportReason", report.getReportReason());
        item.put("reportDetails", report.getReportDetails());
        item.put("reportStatus", report.getReportStatus());
        item.put("handledBy", report.getHandledBy());
        item.put("createdAt", report.getCreatedAt());
        item.put("handledAt", report.getHandledAt());

        userRepository.findById(report.getReporterUserId()).ifPresent(user -> {
            item.put("reporterName", user.getFullName());
            item.put("reporterEmail", user.getEmail());
            item.put("reporterRole", user.getRole());
        });

        if (report.getReportedUserId() != null) {
            userRepository.findById(report.getReportedUserId()).ifPresent(user -> {
                item.put("reportedUserName", user.getFullName());
                item.put("reportedUserEmail", user.getEmail());
                item.put("reportedUserRole", user.getRole());
                item.put("reportedUserStatus", user.getAccountStatus());
            });
        } else {
            item.put("reportedUserName", null);
            item.put("reportedUserEmail", null);
            item.put("reportedUserRole", null);
            item.put("reportedUserStatus", null);
        }

        if (report.getRelatedJobId() != null) {
            jobRepository.findById(report.getRelatedJobId()).ifPresent(job -> {
                item.put("relatedJobTitle", job.getTitle());
                item.put("relatedJobStatus", job.getStatus());
            });
        } else {
            item.put("relatedJobTitle", null);
            item.put("relatedJobStatus", null);
        }

        if (report.getHandledBy() != null) {
            userRepository.findById(report.getHandledBy()).ifPresent(user -> {
                item.put("handledByName", user.getFullName());
                item.put("handledByRole", user.getRole());
            });
        } else {
            item.put("handledByName", null);
            item.put("handledByRole", null);
        }

        return item;
    }

    private void log(Long userId, String actionType, String targetType, Long targetId) {
        AnalyticsLog log = new AnalyticsLog();
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        analyticsLogRepository.save(log);
    }
}