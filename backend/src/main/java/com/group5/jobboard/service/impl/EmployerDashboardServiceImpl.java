package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.EmployerProfile;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.repository.ApplicationRepository;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.service.EmployerDashboardService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployerDashboardServiceImpl implements EmployerDashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public EmployerDashboardServiceImpl(JobRepository jobRepository,
                                        ApplicationRepository applicationRepository,
                                        EmployerProfileRepository employerProfileRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    @Override
    public Map<String, Object> getDashboard(Long employerId) {
        long totalJobs = jobRepository.countByEmployerId(employerId);
        long activeJobs = jobRepository.countByEmployerIdAndStatus(employerId, "active");
        long closedJobs = jobRepository.countByEmployerIdAndStatus(employerId, "closed");
        long deletedJobs = jobRepository.countByEmployerIdAndStatus(employerId, "deleted");

        List<Job> jobs = jobRepository.findByEmployerId(employerId);
        List<Long> jobIds = jobs.stream().map(Job::getId).toList();

        long totalApplications = jobIds.isEmpty() ? 0 : applicationRepository.countByJobIdIn(jobIds);
        long submittedApplications = jobIds.isEmpty() ? 0 : applicationRepository.countByJobIdInAndStatus(jobIds, "submitted");
        long reviewingApplications = jobIds.isEmpty() ? 0 : applicationRepository.countByJobIdInAndStatus(jobIds, "reviewing");
        long acceptedApplications = jobIds.isEmpty() ? 0 : applicationRepository.countByJobIdInAndStatus(jobIds, "accepted");
        long rejectedApplications = jobIds.isEmpty() ? 0 : applicationRepository.countByJobIdInAndStatus(jobIds, "rejected");

        EmployerProfile employerProfile = employerProfileRepository.findByUserId(employerId)
                .orElse(null);

        String verificationStatus = employerProfile == null
                ? "not_submitted"
                : employerProfile.getVerificationStatus();

        Map<String, Object> result = new HashMap<>();
        result.put("totalJobs", totalJobs);
        result.put("activeJobs", activeJobs);
        result.put("closedJobs", closedJobs);
        result.put("deletedJobs", deletedJobs);
        result.put("totalApplications", totalApplications);
        result.put("submittedApplications", submittedApplications);
        result.put("reviewingApplications", reviewingApplications);
        result.put("acceptedApplications", acceptedApplications);
        result.put("rejectedApplications", rejectedApplications);
        result.put("verificationStatus", verificationStatus);

        return result;
    }
}