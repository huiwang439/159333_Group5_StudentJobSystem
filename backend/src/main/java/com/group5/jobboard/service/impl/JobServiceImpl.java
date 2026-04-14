package com.group5.jobboard.service.impl;

import com.group5.jobboard.dto.JobCreateRequest;
import com.group5.jobboard.entity.Job;
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

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
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

        // 为了测试和演示方便，创建后直接设为 active
        job.setStatus("active");

        Job savedJob = jobRepository.save(job);

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", savedJob.getId());
        result.put("employerId", savedJob.getEmployerId());
        result.put("title", savedJob.getTitle());
        result.put("categoryId", savedJob.getCategoryId());
        result.put("description", savedJob.getDescription());
        result.put("requirements", savedJob.getRequirements());
        result.put("employmentType", savedJob.getEmploymentType());
        result.put("workMode", savedJob.getWorkMode());
        result.put("location", savedJob.getLocation());
        result.put("fieldOfStudy", savedJob.getFieldOfStudy());
        result.put("salaryMin", savedJob.getSalaryMin());
        result.put("salaryMax", savedJob.getSalaryMax());
        result.put("deadline", savedJob.getDeadline());
        result.put("status", savedJob.getStatus());
        result.put("createdAt", savedJob.getCreatedAt());
        result.put("updatedAt", savedJob.getUpdatedAt());

        return result;
    }

    @Override
    public List<Map<String, Object>> getPublicJobs() {
        List<Job> jobs = jobRepository.findByStatus("active");
        return buildJobList(jobs);
    }

    @Override
    public Map<String, Object> getJobDetail(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("employerId", job.getEmployerId());
        result.put("title", job.getTitle());
        result.put("categoryId", job.getCategoryId());
        result.put("description", job.getDescription());
        result.put("requirements", job.getRequirements());
        result.put("employmentType", job.getEmploymentType());
        result.put("workMode", job.getWorkMode());
        result.put("location", job.getLocation());
        result.put("fieldOfStudy", job.getFieldOfStudy());
        result.put("salaryMin", job.getSalaryMin());
        result.put("salaryMax", job.getSalaryMax());
        result.put("deadline", job.getDeadline());
        result.put("status", job.getStatus());
        result.put("createdAt", job.getCreatedAt());
        result.put("updatedAt", job.getUpdatedAt());
        return result;
    }

    @Override
    public List<Map<String, Object>> getEmployerJobs(Long employerId) {
        List<Job> jobs = jobRepository.findByEmployerId(employerId);
        return buildJobList(jobs);
    }

    @Override
    public List<Map<String, Object>> searchPublicJobs(String keyword,
                                                      String location,
                                                      String employmentType,
                                                      String fieldOfStudy) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String safeLocation = location == null ? "" : location.trim();
        String safeEmploymentType = employmentType == null ? "" : employmentType.trim();
        String safeFieldOfStudy = fieldOfStudy == null ? "" : fieldOfStudy.trim();

        List<Job> jobs = jobRepository
                .findByStatusAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndEmploymentTypeContainingIgnoreCaseAndFieldOfStudyContainingIgnoreCase(
                        "active",
                        safeKeyword,
                        safeLocation,
                        safeEmploymentType,
                        safeFieldOfStudy
                );

        return buildJobList(jobs);
    }

    private List<Map<String, Object>> buildJobList(List<Job> jobs) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : jobs) {
            Map<String, Object> item = new HashMap<>();
            item.put("jobId", job.getId());
            item.put("title", job.getTitle());
            item.put("location", job.getLocation());
            item.put("employmentType", job.getEmploymentType());
            item.put("workMode", job.getWorkMode());
            item.put("fieldOfStudy", job.getFieldOfStudy());
            item.put("salaryMin", job.getSalaryMin());
            item.put("salaryMax", job.getSalaryMax());
            item.put("deadline", job.getDeadline());
            item.put("status", job.getStatus());
            result.add(item);
        }

        return result;
    }
}