package com.group5.jobboard.service.impl;

import com.group5.jobboard.repository.ApplicationRepository;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.StudentProfileRepository;
import com.group5.jobboard.repository.UserRepository;
import com.group5.jobboard.service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public AnalyticsServiceImpl(UserRepository userRepository,
                                JobRepository jobRepository,
                                ApplicationRepository applicationRepository,
                                StudentProfileRepository studentProfileRepository,
                                EmployerProfileRepository employerProfileRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", userRepository.count());
        result.put("totalStudents", studentProfileRepository.count());
        result.put("totalEmployers", employerProfileRepository.count());
        result.put("totalJobs", jobRepository.count());
        result.put("totalApplications", applicationRepository.count());

        return result;
    }

    @Override
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalJobs", jobRepository.count());
        result.put("pendingJobs", jobRepository.findByStatus("pending").size());
        result.put("approvedJobs", jobRepository.findByStatus("approved").size());
        result.put("rejectedJobs", jobRepository.findByStatus("rejected").size());

        return result;
    }

    @Override
    public Map<String, Object> getApplicationStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalApplications", applicationRepository.count());
        result.put("submittedApplications", applicationRepository.findByStatus("submitted").size());
        result.put("reviewingApplications", applicationRepository.findByStatus("reviewing").size());
        result.put("interviewApplications", applicationRepository.findByStatus("interview").size());
        result.put("acceptedApplications", applicationRepository.findByStatus("accepted").size());
        result.put("rejectedApplications", applicationRepository.findByStatus("rejected").size());

        return result;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", userRepository.count());
        result.put("studentUsers", userRepository.findByRole("student").size());
        result.put("employerUsers", userRepository.findByRole("employer").size());
        result.put("adminUsers", userRepository.findByRole("admin").size());

        return result;
    }
}