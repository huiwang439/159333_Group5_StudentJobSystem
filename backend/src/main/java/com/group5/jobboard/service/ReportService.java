package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface ReportService {

    Map<String, Object> createReport(Long reporterUserId, Long reportedUserId, Long relatedJobId, String reason, String description);

    List<Map<String, Object>> getMyReports(Long reporterUserId);

    List<Map<String, Object>> getAllReports(String reportStatus);

    Map<String, Object> handleReport(Long reportId, String reportStatus, Long adminId);
}