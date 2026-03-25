package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Job;
import com.group5.jobboard.entity.Report;
import com.group5.jobboard.entity.User;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.ReportRepository;
import com.group5.jobboard.repository.UserRepository;
import com.group5.jobboard.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ReportServiceImpl(ReportRepository reportRepository,
                             UserRepository userRepository,
                             JobRepository jobRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public Map<String, Object> createReport(Long reporterUserId, Long reportedUserId, Long relatedJobId,
                                            String reason, String description) {
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        userRepository.findById(reportedUserId)
                .orElseThrow(() -> new RuntimeException("Reported user not found"));

        if (reporter.getId().equals(reportedUserId)) {
            throw new RuntimeException("You cannot report yourself");
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

        reportRepository.save(report);

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("reportStatus", report.getReportStatus());
        result.put("reportedUserId", report.getReportedUserId());

        return result;
    }

    @Override
    public List<Map<String, Object>> getMyReports(Long reporterUserId) {
        List<Report> reports = reportRepository.findByReporterUserId(reporterUserId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Report report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("reportId", report.getId());
            item.put("reporterUserId", report.getReporterUserId());
            item.put("reportedUserId", report.getReportedUserId());
            item.put("relatedJobId", report.getRelatedJobId());
            item.put("reportReason", report.getReportReason());
            item.put("reportDetails", report.getReportDetails());
            item.put("reportStatus", report.getReportStatus());
            item.put("createdAt", report.getCreatedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAllReports(String reportStatus) {
        List<Report> reports;

        if (reportStatus == null || reportStatus.isBlank()) {
            reports = reportRepository.findAll();
        } else {
            reports = reportRepository.findByReportStatus(reportStatus);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Report report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("reportId", report.getId());
            item.put("reporterUserId", report.getReporterUserId());
            item.put("reportedUserId", report.getReportedUserId());
            item.put("relatedJobId", report.getRelatedJobId());
            item.put("reportReason", report.getReportReason());
            item.put("reportDetails", report.getReportDetails());
            item.put("reportStatus", report.getReportStatus());
            item.put("handledBy", report.getHandledBy());
            item.put("handledAt", report.getHandledAt());
            item.put("createdAt", report.getCreatedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> handleReport(Long reportId, String reportStatus, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!reportStatus.equals("pending")
                && !reportStatus.equals("reviewed")
                && !reportStatus.equals("resolved")
                && !reportStatus.equals("rejected")) {
            throw new RuntimeException("Invalid report status");
        }

        report.setReportStatus(reportStatus);
        report.setHandledBy(adminId);
        report.setHandledAt(LocalDateTime.now());

        reportRepository.save(report);

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("reportStatus", report.getReportStatus());
        result.put("updated", true);

        return result;
    }
}