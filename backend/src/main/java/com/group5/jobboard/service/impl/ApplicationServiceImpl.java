package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.ApplicationCreateRequest;
import com.group5.jobboard.dto.ApplicationStatusUpdateRequest;
import com.group5.jobboard.entity.AnalyticsLog;
import com.group5.jobboard.entity.Application;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.entity.StudentDocument;
import com.group5.jobboard.repository.AnalyticsLogRepository;
import com.group5.jobboard.repository.ApplicationRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.StudentDocumentRepository;
import com.group5.jobboard.service.ApplicationService;
import com.group5.jobboard.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final AnalyticsLogRepository analyticsLogRepository;
    private final NotificationService notificationService;
    private final StudentDocumentRepository studentDocumentRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  AnalyticsLogRepository analyticsLogRepository,
                                  NotificationService notificationService,
                                  StudentDocumentRepository studentDocumentRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.analyticsLogRepository = analyticsLogRepository;
        this.notificationService = notificationService;
        this.studentDocumentRepository = studentDocumentRepository;
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

        if (request.getResumeDocumentId() == null) {
            throw new RuntimeException("resumeDocumentId is required");
        }

        StudentDocument resume = studentDocumentRepository
                .findByIdAndStudentId(request.getResumeDocumentId(), studentId)
                .orElseThrow(() -> new RuntimeException("Resume document not found"));

        if (!"resume".equals(resume.getDocumentType())) {
            throw new RuntimeException("resumeDocumentId must be a resume document");
        }

        if (request.getPortfolioDocumentId() != null) {
            StudentDocument portfolio = studentDocumentRepository
                    .findByIdAndStudentId(request.getPortfolioDocumentId(), studentId)
                    .orElseThrow(() -> new RuntimeException("Portfolio document not found"));

            if (!"portfolio".equals(portfolio.getDocumentType())) {
                throw new RuntimeException("portfolioDocumentId must be a portfolio document");
            }
        }

        Application application = new Application();
        application.setJobId(request.getJobId());
        application.setStudentId(studentId);
        application.setCoverLetterText(request.getCoverLetterText());
        application.setResumeDocumentId(request.getResumeDocumentId());
        application.setPortfolioDocumentId(request.getPortfolioDocumentId());
        application.setStatus("submitted");

        applicationRepository.save(application);

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

        return applicationToMap(application);
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
            throw new RuntimeException("You are not allowed to update this application");
        }

        String status = request.getStatus();

        if (!status.equals("submitted")
                && !status.equals("reviewing")
                && !status.equals("interview")
                && !status.equals("accepted")
                && !status.equals("rejected")) {
            throw new RuntimeException("Invalid application status");
        }

        application.setStatus(status);
        applicationRepository.save(application);

        log(employerId, "UPDATE_APPLICATION_STATUS", "APPLICATION", application.getId());

        notificationService.createNotification(
                application.getStudentId(),
                "APPLICATION_STATUS",
                "Application status updated",
                "Your application status has been changed to: " + status
        );

        Map<String, Object> result = applicationToMap(application);
        result.put("updated", true);

        return result;
    }

    @Override
    public Map<String, Object> getApplicationResume(Long employerId, String role, Long applicationId) {
        if (!"employer".equals(role)) {
            throw new RuntimeException("Only employer can view student resume");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You can only view resumes for your own job applications");
        }

        Map<String, Object> result = new HashMap<>();

        result.put("applicationId", application.getId());
        result.put("jobId", application.getJobId());
        result.put("jobTitle", job.getTitle());
        result.put("studentId", application.getStudentId());
        result.put("coverLetterText", application.getCoverLetterText());
        result.put("status", application.getStatus());
        result.put("appliedAt", application.getAppliedAt());
        result.put("updatedAt", application.getUpdatedAt());

        if (application.getResumeDocumentId() != null) {
            StudentDocument resume = studentDocumentRepository.findById(application.getResumeDocumentId())
                    .orElseThrow(() -> new RuntimeException("Resume document not found"));

            result.put("resumeDocumentId", resume.getId());
            result.put("resumeFileName", resume.getFileName());
            result.put("resumeFileUrl", resume.getFileUrl());
            result.put("resumeUploadedAt", resume.getUploadedAt());
        } else {
            result.put("resumeDocumentId", null);
            result.put("resumeFileName", null);
            result.put("resumeFileUrl", null);
            result.put("resumeUploadedAt", null);
        }

        if (application.getPortfolioDocumentId() != null) {
            StudentDocument portfolio = studentDocumentRepository.findById(application.getPortfolioDocumentId())
                    .orElseThrow(() -> new RuntimeException("Portfolio document not found"));

            result.put("portfolioDocumentId", portfolio.getId());
            result.put("portfolioFileName", portfolio.getFileName());
            result.put("portfolioFileUrl", portfolio.getFileUrl());
            result.put("portfolioUploadedAt", portfolio.getUploadedAt());
        } else {
            result.put("portfolioDocumentId", null);
            result.put("portfolioFileName", null);
            result.put("portfolioFileUrl", null);
            result.put("portfolioUploadedAt", null);
        }

        log(employerId, "VIEW_STUDENT_RESUME", "APPLICATION", applicationId);

        return result;
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
            studentDocumentRepository.findById(application.getResumeDocumentId()).ifPresent(resume -> {
                item.put("resumeFileName", resume.getFileName());
                item.put("resumeFileUrl", resume.getFileUrl());
            });
        }

        if (application.getPortfolioDocumentId() != null) {
            studentDocumentRepository.findById(application.getPortfolioDocumentId()).ifPresent(portfolio -> {
                item.put("portfolioFileName", portfolio.getFileName());
                item.put("portfolioFileUrl", portfolio.getFileUrl());
            });
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