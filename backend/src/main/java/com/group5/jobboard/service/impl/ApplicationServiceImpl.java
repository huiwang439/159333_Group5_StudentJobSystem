package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.ApplicationCreateRequest;
import com.group5.jobboard.entity.Application;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.repository.ApplicationRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.service.ApplicationService;
import org.springframework.stereotype.Service;
import com.group5.jobboard.dto.ApplicationStatusUpdateRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

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

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", application.getStatus());
        result.put("jobId", job.getId());

        return result;
    }

    @Override
    public List<Map<String, Object>> getMyApplications(Long studentId) {
        List<Application> applications = applicationRepository.findByStudentId(studentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", application.getId());
            item.put("jobId", application.getJobId());
            item.put("studentId", application.getStudentId());
            item.put("status", application.getStatus());
            item.put("appliedAt", application.getAppliedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getApplicationsByJob(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You are not allowed to view applications for this job");
        }

        List<Application> applications = applicationRepository.findByJobId(jobId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application application : applications) {
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", application.getId());
            item.put("jobId", application.getJobId());
            item.put("studentId", application.getStudentId());
            item.put("status", application.getStatus());
            item.put("appliedAt", application.getAppliedAt());
            result.add(item);
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

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("jobId", application.getJobId());
        result.put("studentId", application.getStudentId());
        result.put("coverLetterText", application.getCoverLetterText());
        result.put("status", application.getStatus());
        result.put("appliedAt", application.getAppliedAt());
        result.put("updatedAt", application.getUpdatedAt());

        return result;
    }
    @Override
    public Map<String, Object> updateApplicationStatus(Long employerId, Long applicationId, ApplicationStatusUpdateRequest request) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", application.getStatus());
        result.put("updated", true);

        return result;
    }
}