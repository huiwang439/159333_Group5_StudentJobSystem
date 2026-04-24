package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Application;
import com.group5.jobboard.entity.Job;
import com.group5.jobboard.entity.JobCategory;
import com.group5.jobboard.repository.*;
import com.group5.jobboard.service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final ReportRepository reportRepository;
    private final VerificationRequestRepository verificationRequestRepository;

    public AnalyticsServiceImpl(UserRepository userRepository,
                                JobRepository jobRepository,
                                ApplicationRepository applicationRepository,
                                StudentProfileRepository studentProfileRepository,
                                EmployerProfileRepository employerProfileRepository,
                                JobCategoryRepository jobCategoryRepository,
                                ReportRepository reportRepository,
                                VerificationRequestRepository verificationRequestRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.reportRepository = reportRepository;
        this.verificationRequestRepository = verificationRequestRepository;
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
        result.put("pendingJobs", jobRepository.countByStatus("pending"));
        result.put("approvedJobs", jobRepository.countByStatus("approved"));
        result.put("rejectedJobs", jobRepository.countByStatus("rejected"));
        result.put("removedJobs", jobRepository.countByStatus("removed"));
        result.put("closedJobs", jobRepository.countByStatus("closed"));
        return result;
    }

    @Override
    public Map<String, Object> getApplicationStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalApplications", applicationRepository.count());
        result.put("submittedApplications", applicationRepository.countByStatus("submitted"));
        result.put("reviewingApplications", applicationRepository.countByStatus("reviewing"));
        result.put("interviewApplications", applicationRepository.countByStatus("interview"));
        result.put("acceptedApplications", applicationRepository.countByStatus("accepted"));
        result.put("rejectedApplications", applicationRepository.countByStatus("rejected"));
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

    @Override
    public Map<String, Object> getAnalyticsOverview(int days) {
        if (days <= 0) {
            days = 7;
        }

        Map<String, Object> result = new HashMap<>();

        result.put("jobGrowthTrend", getJobGrowthTrend(days));
        result.put("positionDistribution", getPositionDistribution());
        result.put("applicationTrend", getApplicationTrend(days));
        result.put("pendingReviewCount", getPendingReviewCount());
        result.put("reportStatistics", getReportStatistics());
        result.put("conversionRate", getConversionRate());

        return result;
    }

    private List<Map<String, Object>> getJobGrowthTrend(int days) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            List<Job> jobs = jobRepository.findByCreatedAtBetween(start, end);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", jobs.size());
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> getApplicationTrend(int days) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            List<Application> applications = applicationRepository.findByAppliedAtBetween(start, end);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", applications.size());
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> getPositionDistribution() {
        List<Job> jobs = jobRepository.findAll();
        Map<String, Integer> counter = new LinkedHashMap<>();

        for (Job job : jobs) {
            String categoryName = "Uncategorized";

            if (job.getCategoryId() != null) {
                Optional<JobCategory> category = jobCategoryRepository.findById(job.getCategoryId());
                if (category.isPresent()) {
                    categoryName = category.get().getCategoryName();
                }
            }

            counter.put(categoryName, counter.getOrDefault(categoryName, 0) + 1);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (String name : counter.keySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("value", counter.get(name));
            result.add(item);
        }

        return result;
    }

    private Map<String, Object> getPendingReviewCount() {
        long pendingJobs = jobRepository.countByStatus("pending");
        long pendingVerifications = verificationRequestRepository.countByReviewStatus("pending");
        long pendingReports = reportRepository.countByReportStatus("pending");

        Map<String, Object> result = new HashMap<>();
        result.put("pendingJobs", pendingJobs);
        result.put("pendingVerifications", pendingVerifications);
        result.put("pendingReports", pendingReports);
        result.put("totalPending", pendingJobs + pendingVerifications + pendingReports);

        return result;
    }

    private List<Map<String, Object>> getReportStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        result.add(chartItem("pending", reportRepository.countByReportStatus("pending")));
        result.add(chartItem("resolved", reportRepository.countByReportStatus("resolved")));
        result.add(chartItem("rejected", reportRepository.countByReportStatus("rejected")));

        return result;
    }

    private Map<String, Object> getConversionRate() {
        long totalJobs = jobRepository.count();
        long totalApplications = applicationRepository.count();
        long acceptedApplications = applicationRepository.countByStatus("accepted");

        double applicationPerJobRate = 0;
        double acceptanceRate = 0;

        if (totalJobs > 0) {
            applicationPerJobRate = (double) totalApplications / totalJobs;
        }

        if (totalApplications > 0) {
            acceptanceRate = ((double) acceptedApplications / totalApplications) * 100;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalJobs", totalJobs);
        result.put("totalApplications", totalApplications);
        result.put("acceptedApplications", acceptedApplications);
        result.put("applicationPerJobRate", Math.round(applicationPerJobRate * 100.0) / 100.0);
        result.put("acceptanceRate", Math.round(acceptanceRate * 100.0) / 100.0);

        return result;
    }

    private Map<String, Object> chartItem(String name, long value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}