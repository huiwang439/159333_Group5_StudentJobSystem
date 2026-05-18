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

import java.util.*;

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
        fillJobFromRequest(job, employerId, request);
        job.setStatus("pending");

        Job saved = jobRepository.save(job);

        log(employerId, "CREATE_JOB", "JOB", saved.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", saved.getId());
        result.put("title", saved.getTitle());
        result.put("status", saved.getStatus());
        result.put("targetStudentType", saved.getTargetStudentType());
        result.put("message", "Job submitted and waiting for admin review");

        return result;
    }

    @Override
    public List<Map<String, Object>> getPublicJobs() {
        return jobRepository.findByStatusOrderByCreatedAtDesc("approved")
                .stream()
                .map(this::jobToMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getPublicJobsByStudentType(String studentType) {
        String normalizedStudentType = normalizeStudentTypeForFilter(studentType);

        if ("ALL".equals(normalizedStudentType)) {
            return getPublicJobs();
        }

        List<String> allowedTypes = new ArrayList<>();
        allowedTypes.add("ALL");
        allowedTypes.add(normalizedStudentType);

        return jobRepository.findByStatusAndTargetStudentTypeInOrderByCreatedAtDesc("approved", allowedTypes)
                .stream()
                .map(this::jobToMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> searchPublicJobs(String keyword,
                                                      String location,
                                                      String employmentType,
                                                      String fieldOfStudy) {
        return searchPublicJobs(keyword, location, employmentType, fieldOfStudy, "ALL");
    }

    @Override
    public List<Map<String, Object>> searchPublicJobs(String keyword,
                                                      String location,
                                                      String employmentType,
                                                      String fieldOfStudy,
                                                      String studentType) {
        String normalizedStudentType = normalizeStudentTypeForFilter(studentType);

        List<Job> jobs;

        if ("ALL".equals(normalizedStudentType)) {
            jobs = jobRepository.findByStatusOrderByCreatedAtDesc("approved");
        } else {
            jobs = jobRepository.findByStatusAndTargetStudentTypeInOrderByCreatedAtDesc(
                    "approved",
                    Arrays.asList("ALL", normalizedStudentType)
            );
        }

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
                                || containsIgnoreCase(job.getWorkMode(), k)
                                || containsIgnoreCase(job.getTargetStudentType(), k);

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
        return jobRepository.findByEmployerId(employerId)
                .stream()
                .map(this::jobToMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getEmployerJobs(Long employerId, String status) {
        List<Job> jobs;

        if (status == null || status.isBlank()) {
            jobs = jobRepository.findByEmployerId(employerId);
        } else {
            jobs = jobRepository.findByEmployerIdAndStatus(employerId, status);
        }

        return jobs.stream()
                .map(this::jobToMap)
                .toList();
    }

    @Override
    public Map<String, Object> updateJob(Long employerId, Long jobId, JobCreateRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You are not allowed to update this job");
        }

        fillJobFromRequest(job, employerId, request);

        Job saved = jobRepository.save(job);

        log(employerId, "UPDATE_JOB", "JOB", saved.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", saved.getId());
        result.put("title", saved.getTitle());
        result.put("status", saved.getStatus());
        result.put("targetStudentType", saved.getTargetStudentType());
        result.put("updated", true);

        return result;
    }

    @Override
    public Map<String, Object> deleteJob(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You are not allowed to delete this job");
        }

        job.setStatus("deleted");
        Job saved = jobRepository.save(job);

        log(employerId, "DELETE_JOB", "JOB", saved.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", saved.getId());
        result.put("deleted", true);

        return result;
    }

    @Override
    public Map<String, Object> closeJob(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You are not allowed to close this job");
        }

        job.setStatus("closed");
        Job saved = jobRepository.save(job);

        log(employerId, "CLOSE_JOB", "JOB", saved.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", saved.getId());
        result.put("status", saved.getStatus());
        result.put("closed", true);

        return result;
    }

    private void fillJobFromRequest(Job job, Long employerId, JobCreateRequest request) {
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
        job.setTargetStudentType(normalizeTargetStudentType(request.getTargetStudentType()));
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
        item.put("targetStudentType", job.getTargetStudentType());
        item.put("createdAt", job.getCreatedAt());
        item.put("updatedAt", job.getUpdatedAt());

        if (job.getCategoryId() != null) {
            jobCategoryRepository.findById(job.getCategoryId())
                    .ifPresent(category -> item.put("categoryName", category.getCategoryName()));
        }

        employerProfileRepository.findByUserId(job.getEmployerId()).ifPresent(profile -> {
            item.put("companyName", profile.getCompanyName());
            item.put("companyLogoUrl", profile.getLogoUrl());

            Map<String, Object> employerProfile = new HashMap<>();
            employerProfile.put("employerProfileId", profile.getId());
            employerProfile.put("userId", profile.getUserId());
            employerProfile.put("companyName", profile.getCompanyName());
            employerProfile.put("industry", profile.getIndustry());
            employerProfile.put("companySize", profile.getCompanySize());
            employerProfile.put("website", profile.getWebsite());
            employerProfile.put("location", profile.getLocation());
            employerProfile.put("companyDescription", profile.getCompanyDescription());
            employerProfile.put("contactPerson", profile.getContactPerson());
            employerProfile.put("contactEmail", profile.getContactEmail());
            employerProfile.put("verificationStatus", profile.getVerificationStatus());
            employerProfile.put("logoUrl", profile.getLogoUrl());

            item.put("employerProfile", employerProfile);
        });

        return item;
    }

    private String normalizeTargetStudentType(String targetStudentType) {
        if (targetStudentType == null || targetStudentType.isBlank()) {
            return "ALL";
        }

        String value = targetStudentType.trim().toUpperCase();

        if (!"ALL".equals(value)
                && !"UNDERGRADUATE".equals(value)
                && !"GRADUATE".equals(value)
                && !"RECENT_GRADUATE".equals(value)) {
            throw new RuntimeException("Invalid targetStudentType. Allowed values: ALL, UNDERGRADUATE, GRADUATE, RECENT_GRADUATE");
        }

        return value;
    }

    private String normalizeStudentTypeForFilter(String studentType) {
        if (studentType == null || studentType.isBlank()) {
            return "ALL";
        }

        String value = studentType.trim().toUpperCase();

        if (!"ALL".equals(value)
                && !"UNDERGRADUATE".equals(value)
                && !"GRADUATE".equals(value)
                && !"RECENT_GRADUATE".equals(value)) {
            throw new RuntimeException("Invalid studentType. Allowed values: ALL, UNDERGRADUATE, GRADUATE, RECENT_GRADUATE");
        }

        return value;
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