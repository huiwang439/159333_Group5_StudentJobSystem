package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface AiService {

    Map<String, Object> analyseJobFit(Long studentUserId, Long jobId);

    Map<String, Object> recommendJobs(Long studentUserId);

    List<String> improveResume(Long studentUserId);

    Map<String, Object> getJobAlerts(Long studentUserId);

    Map<String, Object> rankCandidates(Long employerId, Long jobId);

    Map<String, Object> recommendStudents(Long employerId, Long jobId);

    Map<String, Object> improveJobDescription(Map<String, Object> request);

    Map<String, Object> getHiringAnalytics(Long employerId);

    Map<String, Object> moderateJob(Long adminId, Long jobId);

    Map<String, Object> detectFraud(Long adminId);

    Map<String, Object> getPlatformAnalytics(Long adminId);

    Map<String, Object> generateMonthlyReport(Long adminId);

    Map<String, Object> chatWithQwen(Long studentUserId, String message);
}