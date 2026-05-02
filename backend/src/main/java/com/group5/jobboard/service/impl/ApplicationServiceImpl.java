package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.ApplicationCreateRequest;
import com.group5.jobboard.dto.ApplicationStatusUpdateRequest;
import com.group5.jobboard.entity.AnalyticsLog;
import com.group5.jobboard.entity.Application;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.repository.AnalyticsLogRepository;
import com.group5.jobboard.repository.ApplicationRepository;
import com.group5.jobboard.repository.JobRepository;
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
    private final UserRepository userRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  AnalyticsLogRepository analyticsLogRepository,
                                  NotificationService notificationService,
                                  UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.analyticsLogRepository = analyticsLogRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
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

        Application application = new Application();
        application.setJobId(request.getJobId());
        application.setStudentId(studentId);
        application.setCoverLetterText(request.getCoverLetterText());
        application.setStatus("submitted");

        applicationRepository.save(application);

        log(studentId, "SUBMIT_APPLICATION", "APPLICATION", application.getId());

        notificationService.createNotification(
                job.getEmployerId(),
                "NEW_APPLICATION",
                "New application received",
                "A student has applied for your job: " + job.getTitle()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("jobId", job.getId());
        result.put("status", application.getStatus());

        return result;
    }

    // ================= 学生查看自己的申请 =================
    @Override
    public List<Map<String, Object>> getMyApplications(Long studentId) {
        List<Application> applications = applicationRepository.findByStudentId(studentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", application.getId());
            item.put("jobId", application.getJobId());
            item.put("studentId", application.getStudentId());

            addStudentInfo(item, application.getStudentId());

            item.put("status", application.getStatus());
            item.put("appliedAt", application.getAppliedAt());

            result.add(item);
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
            throw new RuntimeException("You are not allowed to view applications for this job");
        }

        List<Application> applications;

        if (status == null || status.isBlank()) {
            applications = applicationRepository.findByJobId(jobId);
        } else {
            applications = applicationRepository.findByJobIdAndStatus(jobId, status);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", application.getId());
            item.put("jobId", application.getJobId());
            item.put("studentId", application.getStudentId());
            addStudentInfo(item, application.getStudentId());
            item.put("status", application.getStatus());
            item.put("appliedAt", application.getAppliedAt());
            result.add(item);
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
    }

    // ================= 更新申请状态 =================
    @Override
    public Map<String, Object> updateApplicationStatus(Long employerId, Long applicationId, ApplicationStatusUpdateRequest request) {

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

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", status);

        return result;
    }

    // ================= 工具方法 =================
    private void addStudentInfo(Map<String, Object> item, Long studentId) {
        userRepository.findById(studentId).ifPresent(user -> {
            item.put("studentName", user.getFullName());
            item.put("studentEmail", user.getEmail());
        });
    }

    private void log(Long userId, String action, String type, Long targetId) {
        AnalyticsLog log = new AnalyticsLog();
        log.setUserId(userId);
        log.setActionType(action);
        log.setTargetType(type);
        log.setTargetId(targetId);
        analyticsLogRepository.save(log);
    }
}