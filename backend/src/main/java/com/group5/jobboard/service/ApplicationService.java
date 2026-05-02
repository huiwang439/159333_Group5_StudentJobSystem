package com.group5.jobboard.service;

import com.group5.jobboard.dto.ApplicationCreateRequest;
import com.group5.jobboard.dto.ApplicationStatusUpdateRequest;

import java.util.List;
import java.util.Map;

public interface ApplicationService {

    Map<String, Object> submitApplication(Long studentId, ApplicationCreateRequest request);

    List<Map<String, Object>> getMyApplications(Long studentId);

    List<Map<String, Object>> getApplicationsByJob(Long employerId, Long jobId);

    List<Map<String, Object>> getApplicationsByJob(Long employerId, Long jobId, String status);

    Map<String, Object> getApplicationDetail(Long userId, String role, Long applicationId);

    Map<String, Object> updateApplicationStatus(Long employerId, Long applicationId, ApplicationStatusUpdateRequest request);

    Map<String, Object> getApplicationResume(Long employerId, String role, Long applicationId);
}