package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Job;
import com.group5.jobboard.entity.SavedJob;
import com.group5.jobboard.entity.StudentProfile;
import com.group5.jobboard.repository.EmployerProfileRepository;
import com.group5.jobboard.repository.JobCategoryRepository;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.SavedJobRepository;
import com.group5.jobboard.repository.StudentProfileRepository;
import com.group5.jobboard.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentProfileRepository studentProfileRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobCategoryRepository jobCategoryRepository;

    public RecommendationServiceImpl(StudentProfileRepository studentProfileRepository,
                                     SavedJobRepository savedJobRepository,
                                     JobRepository jobRepository,
                                     EmployerProfileRepository employerProfileRepository,
                                     JobCategoryRepository jobCategoryRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.jobCategoryRepository = jobCategoryRepository;
    }

    @Override
    public List<Map<String, Object>> getMyRecommendations(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        List<SavedJob> savedJobs = savedJobRepository.findByStudentProfileId(profile.getId());
        List<Job> approvedJobs = jobRepository.findByStatus("approved");

        Set<Long> savedJobIds = new HashSet<>();
        Set<Long> preferredCategoryIds = new HashSet<>();
        Set<String> preferredEmploymentTypes = new HashSet<>();
        Set<String> preferredLocations = new HashSet<>();
        Set<String> preferredFields = new HashSet<>();

        for (SavedJob savedJob : savedJobs) {
            savedJobIds.add(savedJob.getJobId());

            jobRepository.findById(savedJob.getJobId()).ifPresent(job -> {
                if (job.getCategoryId() != null) {
                    preferredCategoryIds.add(job.getCategoryId());
                }
                if (job.getEmploymentType() != null) {
                    preferredEmploymentTypes.add(job.getEmploymentType().toLowerCase());
                }
                if (job.getLocation() != null) {
                    preferredLocations.add(job.getLocation().toLowerCase());
                }
                if (job.getFieldOfStudy() != null) {
                    preferredFields.add(job.getFieldOfStudy().toLowerCase());
                }
            });
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Job job : approvedJobs) {
            if (savedJobIds.contains(job.getId())) {
                continue;
            }

            int score = 0;
            List<String> reasons = new ArrayList<>();

            if (job.getCategoryId() != null && preferredCategoryIds.contains(job.getCategoryId())) {
                score += 30;
                reasons.add("Similar to your saved job categories");
            }

            if (job.getEmploymentType() != null
                    && preferredEmploymentTypes.contains(job.getEmploymentType().toLowerCase())) {
                score += 20;
                reasons.add("Similar employment type to your saved jobs");
            }

            if (job.getLocation() != null
                    && preferredLocations.contains(job.getLocation().toLowerCase())) {
                score += 10;
                reasons.add("Similar location to your saved jobs");
            }

            if (profile.getMajor() != null && job.getFieldOfStudy() != null) {
                String major = profile.getMajor().toLowerCase();
                String jobField = job.getFieldOfStudy().toLowerCase();

                if (jobField.contains(major) || major.contains(jobField)) {
                    score += 25;
                    reasons.add("Matches your major: " + profile.getMajor());
                }
            }

            if (profile.getDegreeLevel() != null) {
                String degree = profile.getDegreeLevel().toLowerCase();

                if (isUndergraduate(degree) && isSuitableForUndergraduate(job)) {
                    score += 10;
                    reasons.add("Suitable for undergraduate students");
                }

                if (isGraduate(degree) && isSuitableForGraduate(job)) {
                    score += 10;
                    reasons.add("Suitable for graduate students");
                }
            }

            if (profile.getPreferredLocation() != null && job.getLocation() != null) {
                if (job.getLocation().toLowerCase().contains(profile.getPreferredLocation().toLowerCase())) {
                    score += 5;
                    reasons.add("Matches your preferred location");
                }
            }

            if (profile.getPreferredJobType() != null && job.getEmploymentType() != null) {
                if (job.getEmploymentType().equalsIgnoreCase(profile.getPreferredJobType())) {
                    score += 5;
                    reasons.add("Matches your preferred job type");
                }
            }

            if (score > 0) {
                Map<String, Object> item = jobToRecommendationMap(job, score, reasons);
                result.add(item);
            }
        }

        result.sort((a, b) -> {
            Integer scoreA = (Integer) a.get("matchScore");
            Integer scoreB = (Integer) b.get("matchScore");
            return scoreB.compareTo(scoreA);
        });

        return result;
    }

    private boolean isUndergraduate(String degree) {
        return degree.contains("bachelor")
                || degree.contains("undergraduate")
                || degree.contains("ug")
                || degree.contains("本科");
    }

    private boolean isGraduate(String degree) {
        return degree.contains("master")
                || degree.contains("graduate")
                || degree.contains("postgraduate")
                || degree.contains("pg")
                || degree.contains("硕士")
                || degree.contains("研究生");
    }

    private boolean isSuitableForUndergraduate(Job job) {
        String text = combineJobText(job);
        return text.contains("intern")
                || text.contains("internship")
                || text.contains("junior")
                || text.contains("assistant")
                || text.contains("entry");
    }

    private boolean isSuitableForGraduate(Job job) {
        String text = combineJobText(job);
        return text.contains("graduate")
                || text.contains("master")
                || text.contains("analyst")
                || text.contains("associate")
                || text.contains("full-time")
                || text.contains("full time");
    }

    private String combineJobText(Job job) {
        return (
                safe(job.getTitle()) + " "
                        + safe(job.getDescription()) + " "
                        + safe(job.getRequirements()) + " "
                        + safe(job.getEmploymentType()) + " "
                        + safe(job.getFieldOfStudy())
        ).toLowerCase();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Map<String, Object> jobToRecommendationMap(Job job, int score, List<String> reasons) {
        Map<String, Object> item = new HashMap<>();

        item.put("jobId", job.getId());
        item.put("title", job.getTitle());
        item.put("employerId", job.getEmployerId());
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
        item.put("matchScore", score);
        item.put("recommendationReasons", reasons);

        employerProfileRepository.findByUserId(job.getEmployerId())
                .ifPresent(profile -> item.put("companyName", profile.getCompanyName()));

        if (job.getCategoryId() != null) {
            jobCategoryRepository.findById(job.getCategoryId())
                    .ifPresent(category -> item.put("categoryName", category.getCategoryName()));
        }

        return item;
    }
}