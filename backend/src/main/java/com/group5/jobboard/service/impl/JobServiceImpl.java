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
        job.setStatus("pending");

        jobRepository.save(job);

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("status", job.getStatus());

        return result;
    }

    @Override
    public List<Map<String, Object>> getPublicJobs() {
        List<Job> jobs = jobRepository.findByStatus("pending");
        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : jobs) {
            Map<String, Object> item = new HashMap<>();
            item.put("jobId", job.getId());
            item.put("title", job.getTitle());
            item.put("employmentType", job.getEmploymentType());
            item.put("workMode", job.getWorkMode());
            item.put("location", job.getLocation());
            item.put("status", job.getStatus());
            result.add(item);
        }

        return result;
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

        return result;
    }

    @Override
    public List<Map<String, Object>> getEmployerJobs(Long employerId) {
        List<Job> jobs = jobRepository.findByEmployerId(employerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : jobs) {
            Map<String, Object> item = new HashMap<>();
            item.put("jobId", job.getId());
            item.put("title", job.getTitle());
            item.put("employmentType", job.getEmploymentType());
            item.put("location", job.getLocation());
            item.put("status", job.getStatus());
            result.add(item);
        }

        return result;
    }
}