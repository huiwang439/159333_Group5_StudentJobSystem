package com.group5.jobboard.service;

import com.group5.jobboard.dto.JobCreateRequest;

import java.util.List;
import java.util.Map;

public interface JobService {

    Map<String, Object> createJob(Long employerId, JobCreateRequest request);

    List<Map<String, Object>> getPublicJobs();

    List<Map<String, Object>> getPublicJobsByStudentType(String studentType);

    Map<String, Object> getJobDetail(Long jobId);

    List<Map<String, Object>> getEmployerJobs(Long employerId);

    List<Map<String, Object>> getEmployerJobs(Long employerId, String status);

    List<Map<String, Object>> searchPublicJobs(String keyword,
                                               String location,
                                               String employmentType,
                                               String fieldOfStudy);

    List<Map<String, Object>> searchPublicJobs(String keyword,
                                               String location,
                                               String employmentType,
                                               String fieldOfStudy,
                                               String studentType);

    Map<String, Object> updateJob(Long employerId, Long jobId, JobCreateRequest request);

    Map<String, Object> deleteJob(Long employerId, Long jobId);

    Map<String, Object> closeJob(Long employerId, Long jobId);
}