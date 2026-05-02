package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.ApplicationCreateRequest;
import com.group5.jobboard.dto.ApplicationStatusUpdateRequest;
import com.group5.jobboard.entity.*;
import com.group5.jobboard.repository.*;
import com.group5.jobboard.service.ApplicationService;
import com.group5.jobboard.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final AnalyticsLogRepository analyticsLogRepository;
    private final NotificationService notificationService;
    private final StudentDocumentRepository studentDocumentRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  AnalyticsLogRepository analyticsLogRepository,
                                  NotificationService notificationService,
                                  StudentDocumentRepository studentDocumentRepository,
                                  ApplicationStatusHistoryRepository applicationStatusHistoryRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.analyticsLogRepository = analyticsLogRepository;
        this.notificationService = notificationService;
        this.studentDocumentRepository = studentDocumentRepository;
        this.applicationStatusHistoryRepository = applicationStatusHistoryRepository;
    }

    @Override
    public Map<String, Object> submitApplication(Long studentId, ApplicationCreateRequest request) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!"approved".equals(job.getStatus())) {
            throw new RuntimeException("You can only apply for approved jobs");
        }

        applicationRepository.findByJobIdAndStudentId(request.getJobId(), studentId)
                .ifPresent(a -> {
                    throw new RuntimeException("You have already applied for this job");
                });

        validateStudentDocument(studentId, request.getResumeDocumentId(), "resume", true);

        if (request.getPortfolioDocumentId() != null) {
            validateStudentDocument(studentId, request.getPortfolioDocumentId(), "portfolio", false);
        }

        Application application = new Application();
        application.setJobId(request.getJobId());
        application.setStudentId(studentId);
        application.setCoverLetterText(request.getCoverLetterText());
        application.setResumeDocumentId(request.getResumeDocumentId());
        application.setPortfolioDocumentId(request.getPortfolioDocumentId());
        application.setStatus("submitted");

        applicationRepository.save(application);

        saveHistory(application.getId(), "submitted", "Application submitted", studentId);

        log(studentId, "SUBMIT_APPLICATION", "APPLICATION", application.getId());

        notificationService.createNotification(
                job.getEmployerId(),
                "NEW_APPLICATION",
                "New application received",
                "A student has applied for your job: " + job.getTitle()
        );

        return applicationToMap(application);
    }

    @Override
    public List<Map<String, Object>> getMyApplications(Long studentId) {
        List<Application> applications = applicationRepository.findByStudentId(studentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
            result.add(applicationToMap(application));
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getApplicationsByJob(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You can only view applications for your own job");
        }

        List<Application> applications = applicationRepository.findByJobId(jobId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
            result.add(applicationToMap(application));
        }

        return result;
    }

    @Override
    public Map<String, Object> getApplicationDetail(Long userId, String role, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean allowed =
                "admin".equals(role)
                        || ("student".equals(role) && application.getStudentId().equals(userId))
                        || ("employer".equals(role) && job.getEmployerId().equals(userId));

        if (!allowed) {
            throw new RuntimeException("You are not allowed to view this application");
        }

        Map<String, Object> result = applicationToMap(application);
        result.put("history", getApplicationHistory(userId, role, applicationId));
        return result;
    }

    @Override
    public Map<String, Object> updateApplicationStatus(Long employerId,
                                                       Long applicationId,
                                                       ApplicationStatusUpdateRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You can only update applications for your own job");
        }

        if (!isValidStatus(request.getStatus())) {
            throw new RuntimeException("Invalid status. Use submitted, reviewing, interview, accepted, or rejected");
        }

        application.setStatus(request.getStatus());
        applicationRepository.save(application);

        saveHistory(applicationId, request.getStatus(), request.getNote(), employerId);

        notificationService.createNotification(
                application.getStudentId(),
                "APPLICATION_STATUS",
                "Application status updated",
                "Your application status has been changed to: " + request.getStatus()
        );

        return applicationToMap(application);
    }

    @Override
    public List<Map<String, Object>> getApplicationHistory(Long userId, String role, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean allowed =
                "admin".equals(role)
                        || ("student".equals(role) && application.getStudentId().equals(userId))
                        || ("employer".equals(role) && job.getEmployerId().equals(userId));

        if (!allowed) {
            throw new RuntimeException("You are not allowed to view application history");
        }

        List<ApplicationStatusHistory> histories =
                applicationStatusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(applicationId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (ApplicationStatusHistory history : histories) {
            Map<String, Object> item = new HashMap<>();
            item.put("historyId", history.getId());
            item.put("applicationId", history.getApplicationId());
            item.put("status", history.getStatus());
            item.put("note", history.getNote());
            item.put("changedBy", history.getChangedBy());
            item.put("changedAt", history.getChangedAt());
            result.add(item);
        }

        return result;
    }

    private void validateStudentDocument(Long studentId, Long documentId, String expectedType, boolean required) {
        if (documentId == null) {
            if (required) {
                throw new RuntimeException(expectedType + "DocumentId is required");
            }
            return;
        }

        StudentDocument document = studentDocumentRepository.findByIdAndStudentId(documentId, studentId)
                .orElseThrow(() -> new RuntimeException("Document not found or does not belong to current student"));

        if (!expectedType.equals(document.getDocumentType())) {
            throw new RuntimeException("Document type must be " + expectedType);
        }
    }

    private void saveHistory(Long applicationId, String status, String note, Long changedBy) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(applicationId);
        history.setStatus(status);
        history.setNote(note);
        history.setChangedBy(changedBy);
        applicationStatusHistoryRepository.save(history);
    }

    private Map<String, Object> applicationToMap(Application application) {
        Map<String, Object> item = new HashMap<>();
        item.put("applicationId", application.getId());
        item.put("jobId", application.getJobId());
        item.put("studentId", application.getStudentId());
        item.put("coverLetterText", application.getCoverLetterText());
        item.put("resumeDocumentId", application.getResumeDocumentId());
        item.put("portfolioDocumentId", application.getPortfolioDocumentId());
        item.put("status", application.getStatus());
        item.put("appliedAt", application.getAppliedAt());
        item.put("updatedAt", application.getUpdatedAt());

        if (application.getResumeDocumentId() != null) {
            studentDocumentRepository.findById(application.getResumeDocumentId()).ifPresent(doc -> {
                item.put("resumeFileName", doc.getFileName());
                item.put("resumeFileUrl", doc.getFileUrl());
            });
        }

        if (application.getPortfolioDocumentId() != null) {
            studentDocumentRepository.findById(application.getPortfolioDocumentId()).ifPresent(doc -> {
                item.put("portfolioFileName", doc.getFileName());
                item.put("portfolioFileUrl", doc.getFileUrl());
            });
        }

        return item;
    }

    private boolean isValidStatus(String status) {
        return "submitted".equals(status)
                || "reviewing".equals(status)
                || "interview".equals(status)
                || "accepted".equals(status)
                || "rejected".equals(status);
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