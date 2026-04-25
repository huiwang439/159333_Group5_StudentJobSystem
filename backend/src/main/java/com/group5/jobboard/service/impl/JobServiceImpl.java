package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.JobCreateRequest;
import com.group5.jobboard.entity.AnalyticsLog;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.repository.AnalyticsLogRepository;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.repository.JobCategoryRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.service.JobService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final AnalyticsLogRepository analyticsLogRepository;

    public JobServiceImpl(JobRepository jobRepository,
                          EmployerProfileRepository employerProfileRepository,
                          JobCategoryRepository jobCategoryRepository,
                          AnalyticsLogRepository analyticsLogRepository) {
        this.jobRepository = jobRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.analyticsLogRepository = analyticsLogRepository;
    }

    @Override
    public Map<String, Object> createJob(Long employerId, JobCreateRequest request) {
        Job job = new Job();
        job.setEmployerId(employerId);
        job.setTitle(request.getTitle());
        job.setCategoryId(request.getCategoryId());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setEmploymentType(request.getEmploymentType());
        job.setWorkMode(request.getWorkMode());
        job.setLocation(request.getLocation());
        job.setFieldOfStudy(request.getFieldOfStudy());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setDeadline(request.getDeadline());

        job.setStatus("pending");

        jobRepository.save(job);

        log(employerId, "CREATE_JOB", "JOB", job.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("title", job.getTitle());
        result.put("status", job.getStatus());
        result.put("message", "Job submitted and waiting for admin review");

        return result;
    }

    @Override
    public List<Map<String, Object>> getPublicJobs() {

        List<Job> jobs = jobRepository.findByStatus("approved");
        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : jobs) {
            result.add(jobToMap(job));
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> searchPublicJobs(String keyword,
                                                      String location,
                                                      String employmentType,
                                                      String fieldOfStudy) {
        List<Job> jobs = jobRepository.findByStatus("approved");
        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : jobs) {
            boolean match = true;

            if (keyword != null && !keyword.isBlank()) {
                String k = keyword.toLowerCase();

                boolean keywordMatch =
                        containsIgnoreCase(job.getTitle(), k)
                                || containsIgnoreCase(job.getDescription(), k)
                                || containsIgnoreCase(job.getRequirements(), k)
                                || containsIgnoreCase(job.getLocation(), k)
                                || containsIgnoreCase(job.getFieldOfStudy(), k)
                                || containsIgnoreCase(job.getEmploymentType(), k)
                                || containsIgnoreCase(job.getWorkMode(), k);

                if (!keywordMatch) {
                    match = false;
                }
            }

            if (location != null && !location.isBlank()) {
                if (!containsIgnoreCase(job.getLocation(), location)) {
                    match = false;
                }
            }

            if (employmentType != null && !employmentType.isBlank()) {
                if (job.getEmploymentType() == null
                        || !job.getEmploymentType().equalsIgnoreCase(employmentType)) {
                    match = false;
                }
            }

            if (fieldOfStudy != null && !fieldOfStudy.isBlank()) {
                if (!containsIgnoreCase(job.getFieldOfStudy(), fieldOfStudy)) {
                    match = false;
                }
            }

            if (match) {
                result.add(jobToMap(job));
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getJobDetail(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!"approved".equals(job.getStatus())) {
            throw new RuntimeException("This job is not available");
        }

        return jobToMap(job);
    }

    @Override
    public List<Map<String, Object>> getEmployerJobs(Long employerId) {

        List<Job> jobs = jobRepository.findByEmployerId(employerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : jobs) {
            result.add(jobToMap(job));
        }

        return result;
    }

    private Map<String, Object> jobToMap(Job job) {
        Map<String, Object> item = new HashMap<>();

        item.put("jobId", job.getId());
        item.put("employerId", job.getEmployerId());
        item.put("title", job.getTitle());
        item.put("categoryId", job.getCategoryId());
        item.put("description", job.getDescription());
        item.put("requirements", job.getRequirements());
        item.put("employmentType", job.getEmploymentType());
        item.put("workMode", job.getWorkMode());
        item.put("location", job.getLocation());
        item.put("fieldOfStudy", job.getFieldOfStudy());
        item.put("salaryMin", job.getSalaryMin());
        item.put("salaryMax", job.getSalaryMax());
        item.put("deadline", job.getDeadline());
        item.put("status", job.getStatus());
        item.put("createdAt", job.getCreatedAt());
        item.put("updatedAt", job.getUpdatedAt());

        employerProfileRepository.findByUserId(job.getEmployerId())
                .ifPresent(profile -> {
                    item.put("companyName", profile.getCompanyName());
                    item.put("industry", profile.getIndustry());
                    item.put("companyLocation", profile.getLocation());
                    item.put("verificationStatus", profile.getVerificationStatus());
                });

        if (job.getCategoryId() != null) {
            jobCategoryRepository.findById(job.getCategoryId())
                    .ifPresent(category -> item.put("categoryName", category.getCategoryName()));
        }

        return item;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || keyword == null) {
            return false;
        }

        return source.toLowerCase().contains(keyword.toLowerCase());
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