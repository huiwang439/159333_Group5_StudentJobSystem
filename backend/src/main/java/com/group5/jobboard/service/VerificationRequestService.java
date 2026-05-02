package com.group5.jobboard.service;

import java.util.List;
import java.util.Map;

public interface VerificationRequestService {

    Map<String, Object> submitRequest(Long employerId, String businessLicenseUrl, String supportingDocumentUrl);

    Map<String, Object> getMyRequest(Long employerId);

    List<Map<String, Object>> getAllRequests(String reviewStatus);

    Map<String, Object> reviewRequest(Long verificationRequestId, Long adminId, String reviewStatus, String reviewNote);
}