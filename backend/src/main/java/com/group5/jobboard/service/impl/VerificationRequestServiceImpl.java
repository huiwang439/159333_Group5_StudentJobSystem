package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.AnalyticsLog;
import com.group5.jobboard.entity.EmployerProfile;
import com.group5.jobboard.entity.VerificationRequest;
import com.group5.jobboard.repository.AnalyticsLogRepository;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.repository.UserRepository;
import com.group5.jobboard.repository.VerificationRequestRepository;
import com.group5.jobboard.service.NotificationService;
import com.group5.jobboard.service.VerificationRequestService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationRequestServiceImpl implements VerificationRequestService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AnalyticsLogRepository analyticsLogRepository;

    public VerificationRequestServiceImpl(VerificationRequestRepository verificationRequestRepository,
                                          EmployerProfileRepository employerProfileRepository,
                                          UserRepository userRepository,
                                          NotificationService notificationService,
                                          AnalyticsLogRepository analyticsLogRepository) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.analyticsLogRepository = analyticsLogRepository;
    }

    @Override
    public Map<String, Object> submitRequest(Long employerId, String businessLicenseUrl, String supportingDocumentUrl) {
        EmployerProfile employerProfile = employerProfileRepository.findByUserId(employerId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        Optional<VerificationRequest> existingRequest =
                verificationRequestRepository.findByEmployerProfileId(employerProfile.getId());

        VerificationRequest verificationRequest = existingRequest.orElseGet(VerificationRequest::new);

        verificationRequest.setEmployerProfileId(employerProfile.getId());
        verificationRequest.setBusinessLicenseUrl(businessLicenseUrl);
        verificationRequest.setSupportingDocumentUrl(supportingDocumentUrl);
        verificationRequest.setReviewStatus("pending");
        verificationRequest.setReviewedBy(null);
        verificationRequest.setReviewNote(null);
        verificationRequest.setReviewedAt(null);

        VerificationRequest saved = verificationRequestRepository.save(verificationRequest);

        employerProfile.setVerificationStatus("pending");
        employerProfileRepository.save(employerProfile);

        log(employerId, "SUBMIT_VERIFICATION", "VERIFICATION", saved.getId());

        return verificationToMap(saved);
    }

    @Override
    public Map<String, Object> submitRequestByFile(Long employerId,
                                                   MultipartFile businessLicenseFile,
                                                   MultipartFile supportingDocumentFile) {
        if (businessLicenseFile == null || businessLicenseFile.isEmpty()) {
            throw new RuntimeException("Business license file is required");
        }

        String businessLicenseUrl = saveVerificationFile(businessLicenseFile);

        String supportingDocumentUrl = null;
        if (supportingDocumentFile != null && !supportingDocumentFile.isEmpty()) {
            supportingDocumentUrl = saveVerificationFile(supportingDocumentFile);
        }

        return submitRequest(employerId, businessLicenseUrl, supportingDocumentUrl);
    }

    @Override
    public Map<String, Object> getMyRequest(Long employerId) {
        EmployerProfile employerProfile = employerProfileRepository.findByUserId(employerId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        VerificationRequest verificationRequest = verificationRequestRepository
                .findByEmployerProfileId(employerProfile.getId())
                .orElseThrow(() -> new RuntimeException("Verification request not found"));

        return verificationToMap(verificationRequest);
    }

    @Override
    public List<Map<String, Object>> getAllRequests(String reviewStatus) {
        List<VerificationRequest> requests;

        if (reviewStatus == null || reviewStatus.isBlank()) {
            requests = verificationRequestRepository.findAllByOrderBySubmittedAtDesc();
        } else {
            requests = verificationRequestRepository.findByReviewStatusOrderBySubmittedAtDesc(reviewStatus);
        }

        return requests.stream()
                .map(this::verificationToMap)
                .toList();
    }

    @Override
    public Map<String, Object> reviewRequest(Long verificationRequestId,
                                             Long reviewerId,
                                             String reviewStatus,
                                             String reviewNote) {
        if (!"approved".equals(reviewStatus) && !"rejected".equals(reviewStatus)) {
            throw new RuntimeException("Invalid review status");
        }

        VerificationRequest request = verificationRequestRepository.findById(verificationRequestId)
                .orElseThrow(() -> new RuntimeException("Verification request not found"));

        EmployerProfile employerProfile = employerProfileRepository.findById(request.getEmployerProfileId())
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        request.setReviewStatus(reviewStatus);
        request.setReviewedBy(reviewerId);
        request.setReviewNote(reviewNote);
        request.setReviewedAt(LocalDateTime.now());

        VerificationRequest saved = verificationRequestRepository.save(request);

        employerProfile.setVerificationStatus(reviewStatus);
        employerProfileRepository.save(employerProfile);

        log(reviewerId, "REVIEW_VERIFICATION", "VERIFICATION", verificationRequestId);

        notificationService.createNotification(
                employerProfile.getUserId(),
                "VERIFICATION_REVIEW",
                "Company verification result",
                "Your company verification has been " + reviewStatus
                        + (reviewNote == null || reviewNote.isBlank() ? "" : ". Note: " + reviewNote)
        );

        return verificationToMap(saved);
    }

    private String saveVerificationFile(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir")
                    + File.separator + "backend"
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources"
                    + File.separator + "static"
                    + File.separator + "uploads"
                    + File.separator + "verification";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;
            File dest = new File(directory, fileName);
            file.transferTo(dest);

            return "/uploads/verification/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload verification file: " + e.getMessage());
        }
    }

    private Map<String, Object> verificationToMap(VerificationRequest request) {
        Map<String, Object> result = new HashMap<>();

        result.put("verificationRequestId", request.getId());
        result.put("employerProfileId", request.getEmployerProfileId());
        result.put("businessLicenseUrl", request.getBusinessLicenseUrl());
        result.put("supportingDocumentUrl", request.getSupportingDocumentUrl());
        result.put("reviewStatus", request.getReviewStatus());
        result.put("reviewedBy", request.getReviewedBy());
        result.put("reviewNote", request.getReviewNote());
        result.put("submittedAt", request.getSubmittedAt());
        result.put("reviewedAt", request.getReviewedAt());

        employerProfileRepository.findById(request.getEmployerProfileId()).ifPresent(profile -> {
            result.put("userId", profile.getUserId());
            result.put("companyName", profile.getCompanyName());
            result.put("industry", profile.getIndustry());
            result.put("location", profile.getLocation());
            result.put("contactPerson", profile.getContactPerson());
            result.put("contactEmail", profile.getContactEmail());
            result.put("verificationStatus", profile.getVerificationStatus());

            userRepository.findById(profile.getUserId()).ifPresent(user -> {
                result.put("employerName", user.getFullName());
                result.put("employerEmail", user.getEmail());
                result.put("accountStatus", user.getAccountStatus());
            });
        });

        if (request.getReviewedBy() != null) {
            userRepository.findById(request.getReviewedBy()).ifPresent(reviewer -> {
                result.put("reviewedByName", reviewer.getFullName());
                result.put("reviewedByRole", reviewer.getRole());
            });
        } else {
            result.put("reviewedByName", null);
            result.put("reviewedByRole", null);
        }

        return result;
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