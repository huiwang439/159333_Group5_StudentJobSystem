package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.Job;
import com.group5.jobboard.entity.Recommendation;
import com.group5.jobboard.entity.StudentProfile;
import com.group5.jobboard.repository.JobRepository;
import com.group5.jobboard.repository.RecommendationRepository;
import com.group5.jobboard.repository.StudentProfileRepository;
import com.group5.jobboard.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;

    public RecommendationServiceImpl(RecommendationRepository recommendationRepository,
                                     StudentProfileRepository studentProfileRepository,
                                     JobRepository jobRepository) {
        this.recommendationRepository = recommendationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public List<Map<String, Object>> getMyRecommendations(Long userId) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        List<Recommendation> recommendations =
                recommendationRepository.findByStudentProfileIdOrderByMatchScoreDesc(studentProfile.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            Job job = jobRepository.findById(recommendation.getJobId()).orElse(null);
            if (job == null) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("recommendationId", recommendation.getId());
            item.put("studentProfileId", recommendation.getStudentProfileId());
            item.put("jobId", job.getId());
            item.put("title", job.getTitle());
            item.put("location", job.getLocation());
            item.put("employmentType", job.getEmploymentType());
            item.put("workMode", job.getWorkMode());
            item.put("fieldOfStudy", job.getFieldOfStudy());
            item.put("matchScore", recommendation.getMatchScore());
            item.put("recommendationReason", recommendation.getRecommendationReason());
            item.put("createdAt", recommendation.getCreatedAt());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> refreshMyRecommendations(Long userId) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        List<Job> jobs = jobRepository.findByStatus("pending");
        recommendationRepository.deleteByStudentProfileId(studentProfile.getId());

        for (Job job : jobs) {
            BigDecimal score = calculateMatchScore(studentProfile, job);
            if (score.compareTo(new BigDecimal("30.00")) < 0) {
                continue;
            }

            Recommendation recommendation = new Recommendation();
            recommendation.setStudentProfileId(studentProfile.getId());
            recommendation.setJobId(job.getId());
            recommendation.setMatchScore(score);
            recommendation.setRecommendationReason(buildReason(studentProfile, job, score));
            recommendationRepository.save(recommendation);
        }

        return getMyRecommendations(userId);
    }

    private BigDecimal calculateMatchScore(StudentProfile studentProfile, Job job) {
        double score = 0.0;

        if (containsIgnoreCase(job.getFieldOfStudy(), studentProfile.getMajor())) {
            score += 40;
        }

        if (containsIgnoreCase(job.getLocation(), studentProfile.getPreferredLocation())) {
            score += 20;
        }

        if (containsIgnoreCase(job.getEmploymentType(), studentProfile.getPreferredJobType())) {
            score += 20;
        }

        int skillMatches = countSkillMatches(studentProfile.getSkills(), job.getRequirements());
        score += Math.min(skillMatches * 10, 20);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildReason(StudentProfile studentProfile, Job job, BigDecimal score) {
        List<String> reasons = new ArrayList<>();

        if (containsIgnoreCase(job.getFieldOfStudy(), studentProfile.getMajor())) {
            reasons.add("major matches field of study");
        }
        if (containsIgnoreCase(job.getLocation(), studentProfile.getPreferredLocation())) {
            reasons.add("location matches preference");
        }
        if (containsIgnoreCase(job.getEmploymentType(), studentProfile.getPreferredJobType())) {
            reasons.add("job type matches preference");
        }
        if (countSkillMatches(studentProfile.getSkills(), job.getRequirements()) > 0) {
            reasons.add("skills match job requirements");
        }

        if (reasons.isEmpty()) {
            reasons.add("general profile similarity");
        }

        return String.join("; ", reasons) + " (score=" + score + ")";
    }

    private int countSkillMatches(String studentSkills, String jobRequirements) {
        if (studentSkills == null || studentSkills.isBlank() || jobRequirements == null || jobRequirements.isBlank()) {
            return 0;
        }

        String[] skills = studentSkills.split(",");
        int count = 0;

        for (String skill : skills) {
            String trimmed = skill.trim();
            if (!trimmed.isEmpty() && containsIgnoreCase(jobRequirements, trimmed)) {
                count++;
            }
        }

        return count;
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (source == null || target == null) {
            return false;
        }
        return source.toLowerCase().contains(target.toLowerCase());
    }
}