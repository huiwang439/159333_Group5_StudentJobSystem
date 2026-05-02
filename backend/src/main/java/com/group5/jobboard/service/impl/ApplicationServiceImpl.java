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
<<<<<<< HEAD
import com.group5.jobboard.repository.StudentDocumentRepository;
=======
import com.group5.jobboard.repository.UserRepository;
>>>>>>> origin/branch-ZhengZihui
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
<<<<<<< HEAD
    private final StudentDocumentRepository studentDocumentRepository;
=======
    private final UserRepository userRepository;
>>>>>>> origin/branch-ZhengZihui

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  AnalyticsLogRepository analyticsLogRepository,
                                  NotificationService notificationService,
<<<<<<< HEAD
                                  StudentDocumentRepository studentDocumentRepository) {
=======
                                  UserRepository userRepository) {
>>>>>>> origin/branch-ZhengZihui
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.analyticsLogRepository = analyticsLogRepository;
        this.notificationService = notificationService;
<<<<<<< HEAD
        this.studentDocumentRepository = studentDocumentRepository;
=======
        this.userRepository = userRepository;
>>>>>>> origin/branch-ZhengZihui
    }

    // ================= 提交申请 =================
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

<<<<<<< HEAD
        return applicationToMap(application);
=======
        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("jobId", job.getId());
        result.put("status", application.getStatus());

        return result;
>>>>>>> origin/branch-ZhengZihui
    }

    // ================= 学生查看自己的申请 =================
    @Override
    public List<Map<String, Object>> getMyApplications(Long studentId) {
        List<Application> applications = applicationRepository.findByStudentId(studentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
<<<<<<< HEAD
            result.add(applicationToMap(application));
=======
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", application.getId());
            item.put("jobId", application.getJobId());
            item.put("studentId", application.getStudentId());

            addStudentInfo(item, application.getStudentId());

            item.put("status", application.getStatus());
            item.put("appliedAt", application.getAppliedAt());

            result.add(item);
>>>>>>> origin/branch-ZhengZihui
        }

        return result;
    }

    // ================= 企业查看岗位申请（✔修复签名问题） =================
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

        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
<<<<<<< HEAD
            result.add(applicationToMap(application));
=======
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", application.getId());
            item.put("jobId", application.getJobId());
            item.put("studentId", application.getStudentId());
            addStudentInfo(item, application.getStudentId());
            item.put("status", application.getStatus());
            item.put("appliedAt", application.getAppliedAt());
            result.add(item);
>>>>>>> origin/branch-ZhengZihui
        }

        return result;
    }
    // ================= 申请详情 =================
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

<<<<<<< HEAD
        return applicationToMap(application);
=======
        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("jobId", application.getJobId());
        result.put("jobTitle", job.getTitle());
        result.put("studentId", application.getStudentId());

        addStudentInfo(result, application.getStudentId());

        result.put("coverLetterText", application.getCoverLetterText());
        result.put("status", application.getStatus());
        result.put("appliedAt", application.getAppliedAt());
        result.put("updatedAt", application.getUpdatedAt());

        return result;
>>>>>>> origin/branch-ZhengZihui
    }

    // ================= 更新申请状态 =================
    @Override
<<<<<<< HEAD
    public Map<String, Object> updateApplicationStatus(Long employerId,
                                                       Long applicationId,
                                                       ApplicationStatusUpdateRequest request) {
=======
    public Map<String, Object> updateApplicationStatus(Long employerId, Long applicationId, ApplicationStatusUpdateRequest request) {

>>>>>>> origin/branch-ZhengZihui
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
        applicationRepository.save(application);

        log(employerId, "UPDATE_APPLICATION_STATUS", "APPLICATION", application.getId());

        notificationService.createNotification(
                application.getStudentId(),
                "APPLICATION_STATUS",
                "Application updated",
                "Your application is now: " + status
        );

<<<<<<< HEAD
        Map<String, Object> result = applicationToMap(application);
        result.put("updated", true);
=======
        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", status);
>>>>>>> origin/branch-ZhengZihui

        return result;
    }

<<<<<<< HEAD
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
=======
    // ================= 工具方法 =================
    private void addStudentInfo(Map<String, Object> item, Long studentId) {
        userRepository.findById(studentId).ifPresent(user -> {
            item.put("studentName", user.getFullName());
            item.put("studentEmail", user.getEmail());
        });
    }

    private void log(Long userId, String action, String type, Long targetId) {
>>>>>>> origin/branch-ZhengZihui
        AnalyticsLog log = new AnalyticsLog();
        log.setUserId(userId);
        log.setActionType(action);
        log.setTargetType(type);
        log.setTargetId(targetId);
        analyticsLogRepository.save(log);
    }
}