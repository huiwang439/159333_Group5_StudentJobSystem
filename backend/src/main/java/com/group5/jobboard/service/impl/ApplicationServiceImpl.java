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
import com.group5.jobboard.repository.StudentProfileRepository;
import com.group5.jobboard.repository.UserRepository;
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
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  AnalyticsLogRepository analyticsLogRepository,
                                  NotificationService notificationService,
                                  StudentDocumentRepository studentDocumentRepository,
                                  StudentProfileRepository studentProfileRepository,
                                  UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.analyticsLogRepository = analyticsLogRepository;
        this.notificationService = notificationService;
        this.studentDocumentRepository = studentDocumentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> submitApplication(Long studentId, ApplicationCreateRequest request) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

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

        if (!"resume".equalsIgnoreCase(resume.getDocumentType())) {
            throw new RuntimeException("resumeDocumentId must be a resume document");
        }

        if (request.getPortfolioDocumentId() != null) {
            StudentDocument portfolio = studentDocumentRepository
                    .findByIdAndStudentId(request.getPortfolioDocumentId(), studentId)
                    .orElseThrow(() -> new RuntimeException("Portfolio document not found"));

            if (!"portfolio".equalsIgnoreCase(portfolio.getDocumentType())) {
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

        Application saved = applicationRepository.save(application);

        log(studentId, "SUBMIT_APPLICATION", "APPLICATION", saved.getId());

        notificationService.createNotification(
                job.getEmployerId(),
                "NEW_APPLICATION",
                "New application received",
                "A student has applied for your job: " + job.getTitle()
        );

        return applicationToMap(saved);
    }

    @Override
    public List<Map<String, Object>> getMyApplications(Long studentId) {
        return applicationRepository.findByStudentId(studentId)
                .stream()
                .map(this::applicationToMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getApplicationsByJob(Long employerId, Long jobId) {
        return getApplicationsByJob(employerId, jobId, null);
    }

    @Override
    public List<Map<String, Object>> getApplicationsByJob(Long employerId, Long jobId, String status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You can only view applications for your own job");
        }

        List<Application> applications;

        if (status == null || status.isBlank()) {
            applications = applicationRepository.findByJobId(jobId);
        } else {
            applications = applicationRepository.findByJobIdAndStatus(jobId, status);
        }

        return applications.stream()
                .map(this::applicationToMap)
                .toList();
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
            throw new RuntimeException("No permission");
        }

        Map<String, Object> result = applicationToMap(application);
        result.put("jobTitle", job.getTitle());
        result.put("jobLocation", job.getLocation());
        result.put("employmentType", job.getEmploymentType());
        result.put("targetStudentType", job.getTargetStudentType());

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
            throw new RuntimeException("No permission");
        }

        String status = request.getStatus();

        if (!List.of("submitted", "reviewing", "accepted", "rejected").contains(status)) {
            throw new RuntimeException("Invalid status");
        }

        application.setStatus(status);
        Application saved = applicationRepository.save(application);

        log(employerId, "UPDATE_APPLICATION_STATUS", "APPLICATION", saved.getId());

        notificationService.createNotification(
                application.getStudentId(),
                "APPLICATION_STATUS",
                "Application updated",
                "Your application is now: " + status
        );

        Map<String, Object> result = applicationToMap(saved);
        result.put("updated", true);

        return result;
    }

    @Override
    public Map<String, Object> getApplicationResume(Long employerId, String role, Long applicationId) {
        if (!"employer".equals(role) && !"admin".equals(role)) {
            throw new RuntimeException("Only employer or admin can view student resume");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if ("employer".equals(role) && !job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You can only view resumes for your own job applications");
        }

        Map<String, Object> result = applicationToMap(application);
        result.put("jobTitle", job.getTitle());

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

        addStudentInfo(item, application.getStudentId());
        addDocumentInfo(item, application);

        return item;
    }

    private void addStudentInfo(Map<String, Object> item, Long studentId) {
        userRepository.findById(studentId).ifPresent(user -> {
            item.put("studentName", user.getFullName());
            item.put("studentEmail", user.getEmail());
            item.put("studentPhone", user.getPhone());
            item.put("studentAccountStatus", user.getAccountStatus());
        });

        studentProfileRepository.findByUserId(studentId).ifPresent(profile -> {
            item.put("studentProfileId", profile.getId());
            item.put("studentUniversity", profile.getUniversity());
            item.put("studentMajor", profile.getMajor());
            item.put("studentDegreeLevel", profile.getDegreeLevel());
            item.put("studentGraduationYear", profile.getGraduationYear());
            item.put("studentSkills", profile.getSkills());
            item.put("studentBio", profile.getBio());
            item.put("studentPreferredLocation", profile.getPreferredLocation());
            item.put("studentPreferredJobType", profile.getPreferredJobType());
            item.put("studentType", profile.getStudentType());
        });
    }

    private void addDocumentInfo(Map<String, Object> item, Application application) {
        if (application.getResumeDocumentId() != null) {
            studentDocumentRepository.findById(application.getResumeDocumentId()).ifPresent(resume -> {
                item.put("resumeFileName", resume.getFileName());
                item.put("resumeFileUrl", resume.getFileUrl());
                item.put("resumeUrl", resume.getFileUrl());
                item.put("resumeUploadedAt", resume.getUploadedAt());
            });
        }

        if (application.getPortfolioDocumentId() != null) {
            studentDocumentRepository.findById(application.getPortfolioDocumentId()).ifPresent(portfolio -> {
                item.put("portfolioFileName", portfolio.getFileName());
                item.put("portfolioFileUrl", portfolio.getFileUrl());
                item.put("portfolioUrl", portfolio.getFileUrl());
                item.put("portfolioUploadedAt", portfolio.getUploadedAt());
            });
        }
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