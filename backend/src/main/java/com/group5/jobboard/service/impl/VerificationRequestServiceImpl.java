package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.EmployerProfile;
import com.group5.jobboard.entity.VerificationRequest;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.repository.VerificationRequestRepository;
import com.group5.jobboard.service.VerificationRequestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VerificationRequestServiceImpl implements VerificationRequestService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public VerificationRequestServiceImpl(VerificationRequestRepository verificationRequestRepository,
                                          EmployerProfileRepository employerProfileRepository) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    @Override
    public Map<String, Object> submitRequest(Long employerId, String businessLicenseUrl, String supportingDocumentUrl) {
        EmployerProfile employerProfile = employerProfileRepository.findByUserId(employerId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        verificationRequestRepository.findByEmployerProfileId(employerProfile.getId())
                .ifPresent(request -> {
                    throw new RuntimeException("Verification request already exists");
                });

        VerificationRequest verificationRequest = new VerificationRequest();
        verificationRequest.setEmployerProfileId(employerProfile.getId());
        verificationRequest.setBusinessLicenseUrl(businessLicenseUrl);
        verificationRequest.setSupportingDocumentUrl(supportingDocumentUrl);
        verificationRequest.setReviewStatus("pending");

        verificationRequestRepository.save(verificationRequest);

        employerProfile.setVerificationStatus("pending");
        employerProfileRepository.save(employerProfile);

        Map<String, Object> result = new HashMap<>();
        result.put("verificationRequestId", verificationRequest.getId());
        result.put("employerProfileId", verificationRequest.getEmployerProfileId());
        result.put("reviewStatus", verificationRequest.getReviewStatus());

        return result;
    }

    @Override
    public Map<String, Object> getMyRequest(Long employerId) {
        EmployerProfile employerProfile = employerProfileRepository.findByUserId(employerId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        VerificationRequest verificationRequest = verificationRequestRepository.findByEmployerProfileId(employerProfile.getId())
                .orElseThrow(() -> new RuntimeException("Verification request not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("verificationRequestId", verificationRequest.getId());
        result.put("employerProfileId", verificationRequest.getEmployerProfileId());
        result.put("businessLicenseUrl", verificationRequest.getBusinessLicenseUrl());
        result.put("supportingDocumentUrl", verificationRequest.getSupportingDocumentUrl());
        result.put("reviewStatus", verificationRequest.getReviewStatus());
        result.put("reviewNote", verificationRequest.getReviewNote());
        result.put("submittedAt", verificationRequest.getSubmittedAt());
        result.put("reviewedAt", verificationRequest.getReviewedAt());

        return result;
    }

    @Override
    public List<Map<String, Object>> getAllRequests(String reviewStatus) {
        List<VerificationRequest> requests;

        if (reviewStatus == null || reviewStatus.isBlank()) {
            requests = verificationRequestRepository.findAll();
        } else {
            requests = verificationRequestRepository.findByReviewStatus(reviewStatus);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (VerificationRequest request : requests) {
            Map<String, Object> item = new HashMap<>();
            item.put("verificationRequestId", request.getId());
            item.put("employerProfileId", request.getEmployerProfileId());
            item.put("businessLicenseUrl", request.getBusinessLicenseUrl());
            item.put("supportingDocumentUrl", request.getSupportingDocumentUrl());
            item.put("reviewStatus", request.getReviewStatus());
            item.put("reviewNote", request.getReviewNote());
            item.put("submittedAt", request.getSubmittedAt());
            item.put("reviewedAt", request.getReviewedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> reviewRequest(Long verificationRequestId, Long adminId, String reviewStatus, String reviewNote) {
        VerificationRequest verificationRequest = verificationRequestRepository.findById(verificationRequestId)
                .orElseThrow(() -> new RuntimeException("Verification request not found"));

        if (!reviewStatus.equals("pending")
                && !reviewStatus.equals("approved")
                && !reviewStatus.equals("rejected")) {
            throw new RuntimeException("Invalid review status");
        }

        EmployerProfile employerProfile = employerProfileRepository.findById(verificationRequest.getEmployerProfileId())
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        verificationRequest.setReviewStatus(reviewStatus);
        verificationRequest.setReviewedBy(adminId);
        verificationRequest.setReviewNote(reviewNote);
        verificationRequest.setReviewedAt(LocalDateTime.now());

        verificationRequestRepository.save(verificationRequest);

        employerProfile.setVerificationStatus(reviewStatus);
        employerProfileRepository.save(employerProfile);

        Map<String, Object> result = new HashMap<>();
        result.put("verificationRequestId", verificationRequest.getId());
        result.put("reviewStatus", verificationRequest.getReviewStatus());
        result.put("reviewedBy", verificationRequest.getReviewedBy());
        result.put("updated", true);

        return result;
    }
}