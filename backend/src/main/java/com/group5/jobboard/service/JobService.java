package com.group5.jobboard.service;

import com.group5.jobboard.dto.JobCreateRequest;
import java.util.List;
import java.util.Map;

public interface JobService {

    Map<String, Object> createJob(Long employerId, JobCreateRequest request);

    List<Map<String, Object>> getPublicJobs();

    Map<String, Object> getJobDetail(Long jobId);

    List<Map<String, Object>> getEmployerJobs(Long employerId);

    List<Map<String, Object>> searchPublicJobs(String keyword, String location, String employmentType, String fieldOfStudy);
}