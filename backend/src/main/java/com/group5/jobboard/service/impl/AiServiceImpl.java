package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.*;
import com.group5.jobboard.repository.*;
import com.group5.jobboard.service.AiService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final AnalyticsLogRepository analyticsLogRepository;

    public AiServiceImpl(JobRepository jobRepository,
                         StudentProfileRepository studentProfileRepository,
                         ApplicationRepository applicationRepository,
                         UserRepository userRepository,
                         EmployerProfileRepository employerProfileRepository,
                         AnalyticsLogRepository analyticsLogRepository) {
        this.jobRepository = jobRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.analyticsLogRepository = analyticsLogRepository;
    }

    @Override
    public Map<String, Object> analyseJobFit(Long studentUserId, Long jobId) {
        StudentProfile student = getStudentProfileByUserId(studentUserId);
        Job job = getApprovedJob(jobId);

        return buildJobFitAnalysis(student, job);
    }

    @Override
    public Map<String, Object> recommendJobs(Long studentUserId) {
        StudentProfile student = getStudentProfileByUserId(studentUserId);
        List<Job> jobs = jobRepository.findByStatus("approved");

        List<Map<String, Object>> recommendedJobs = new ArrayList<>();

        for (Job job : jobs) {
            Map<String, Object> analysis = buildJobFitAnalysis(student, job);

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
            item.put("matchScore", analysis.get("matchScore"));
            item.put("matchLevel", analysis.get("matchLevel"));
            item.put("whyMatch", analysis.get("whyMatch"));

            recommendedJobs.add(item);
        }

        recommendedJobs.sort((a, b) ->
                Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore"))
        );

        Map<String, Object> result = new HashMap<>();
        result.put("studentProfileId", student.getId());
        result.put("recommendedJobs", recommendedJobs.stream().limit(5).toList());
        result.put("resumeSuggestions", buildResumeSuggestions(student));
        result.put("message", "AI job recommendations generated successfully");

        return result;
    }

    @Override
    public List<String> improveResume(Long studentUserId) {
        StudentProfile student = getStudentProfileByUserId(studentUserId);
        return buildResumeSuggestions(student);
    }

    @Override
    public Map<String, Object> getJobAlerts(Long studentUserId) {
        StudentProfile student = getStudentProfileByUserId(studentUserId);
        List<Job> jobs = jobRepository.findByStatus("approved");

        List<Map<String, Object>> alerts = new ArrayList<>();

        for (Job job : jobs) {
            Map<String, Object> analysis = buildJobFitAnalysis(student, job);
            Integer score = (Integer) analysis.get("matchScore");

            if (score >= 60) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("jobId", job.getId());
                alert.put("title", job.getTitle());
                alert.put("location", job.getLocation());
                alert.put("matchScore", score);
                alert.put("message", "New job matches your profile!");
                alerts.add(alert);
            }
        }

        alerts.sort((a, b) ->
                Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore"))
        );

        Map<String, Object> result = new HashMap<>();
        result.put("studentProfileId", student.getId());
        result.put("alerts", alerts);
        result.put("totalAlerts", alerts.size());

        return result;
    }

    @Override
    public Map<String, Object> rankCandidates(Long employerId, Long jobId) {
        Job job = getEmployerJob(employerId, jobId);
        List<Application> applications = applicationRepository.findByJobId(jobId);

        List<Map<String, Object>> rankedCandidates = new ArrayList<>();

        for (Application application : applications) {
            Optional<StudentProfile> studentOptional =
                    studentProfileRepository.findByUserId(application.getStudentId());

            if (studentOptional.isEmpty()) {
                continue;
            }

            StudentProfile student = studentOptional.get();
            Map<String, Object> analysis = buildJobFitAnalysis(student, job);

            Map<String, Object> candidate = new HashMap<>();
            candidate.put("applicationId", application.getId());
            candidate.put("studentUserId", application.getStudentId());
            candidate.put("studentProfileId", student.getId());
            candidate.put("major", student.getMajor());
            candidate.put("skills", student.getSkills());
            candidate.put("matchScore", analysis.get("matchScore"));
            candidate.put("matchLevel", analysis.get("matchLevel"));
            candidate.put("whyMatch", analysis.get("whyMatch"));
            candidate.put("applicationStatus", application.getStatus());
            candidate.put("appliedAt", application.getAppliedAt());

            userRepository.findById(application.getStudentId()).ifPresent(user -> {
                candidate.put("studentName", user.getFullName());
                candidate.put("studentEmail", user.getEmail());
            });

            rankedCandidates.add(candidate);
        }

        rankedCandidates.sort((a, b) ->
                Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore"))
        );

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("jobTitle", job.getTitle());
        result.put("totalApplicants", applications.size());
        result.put("topCandidates", rankedCandidates);

        return result;
    }

    @Override
    public Map<String, Object> recommendStudents(Long employerId, Long jobId) {
        Job job = getEmployerJob(employerId, jobId);
        List<StudentProfile> students = studentProfileRepository.findAll();

        List<Map<String, Object>> recommendedStudents = new ArrayList<>();

        for (StudentProfile student : students) {
            Map<String, Object> analysis = buildJobFitAnalysis(student, job);
            Integer score = (Integer) analysis.get("matchScore");

            if (score >= 40) {
                Map<String, Object> item = new HashMap<>();
                item.put("studentProfileId", student.getId());
                item.put("studentUserId", student.getUserId());
                item.put("major", student.getMajor());
                item.put("skills", student.getSkills());
                item.put("preferredLocation", student.getPreferredLocation());
                item.put("preferredJobType", student.getPreferredJobType());
                item.put("matchScore", score);
                item.put("matchLevel", analysis.get("matchLevel"));
                item.put("whyMatch", analysis.get("whyMatch"));

                userRepository.findById(student.getUserId()).ifPresent(user -> {
                    item.put("studentName", user.getFullName());
                    item.put("studentEmail", user.getEmail());
                });

                recommendedStudents.add(item);
            }
        }

        recommendedStudents.sort((a, b) ->
                Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore"))
        );

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("jobTitle", job.getTitle());
        result.put("recommendedStudents", recommendedStudents.stream().limit(10).toList());

        return result;
    }

    @Override
    public Map<String, Object> improveJobDescription(Map<String, Object> request) {
        String title = safe((String) request.get("title"));
        String description = safe((String) request.get("description"));
        String requirements = safe((String) request.get("requirements"));

        List<String> keywords = extractJobKeywords(title + " " + description + " " + requirements);

        String improvedDescription =
                "We are seeking a motivated " + (title.isBlank() ? "candidate" : title) +
                        " to join our team. The ideal candidate should have strong communication skills, " +
                        "a willingness to learn, and relevant experience in " +
                        (keywords.isEmpty() ? "the related field" : String.join(", ", keywords)) +
                        ". This role provides an opportunity to develop practical skills, contribute to real projects, " +
                        "and grow in a professional working environment.";

        List<String> suggestions = new ArrayList<>();

        if (description.length() < 80) {
            suggestions.add("Add more details about daily responsibilities.");
        }

        if (requirements.length() < 50) {
            suggestions.add("Add clearer skill requirements and qualification expectations.");
        }

        suggestions.add("Include keywords such as teamwork, communication, problem solving and project experience.");
        suggestions.add("Mention career development opportunities to attract students.");

        Map<String, Object> result = new HashMap<>();
        result.put("originalTitle", title);
        result.put("originalDescription", description);
        result.put("improvedDescription", improvedDescription);
        result.put("recommendedKeywords", keywords);
        result.put("suggestions", suggestions);

        return result;
    }

    @Override
    public Map<String, Object> getHiringAnalytics(Long employerId) {
        List<Job> jobs = jobRepository.findByEmployerId(employerId);

        int totalJobs = jobs.size();
        int totalApplicants = 0;
        int approvedJobs = 0;
        int closedJobs = 0;

        List<Map<String, Object>> jobAnalytics = new ArrayList<>();

        for (Job job : jobs) {
            List<Application> applications = applicationRepository.findByJobId(job.getId());
            totalApplicants += applications.size();

            if ("approved".equals(job.getStatus())) {
                approvedJobs++;
            }

            if ("closed".equals(job.getStatus())) {
                closedJobs++;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("jobId", job.getId());
            item.put("title", job.getTitle());
            item.put("status", job.getStatus());
            item.put("numberOfApplicants", applications.size());
            item.put("bestFitCandidates", calculateBestFitCandidateCount(job, applications));
            jobAnalytics.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("employerId", employerId);
        result.put("totalJobs", totalJobs);
        result.put("approvedJobs", approvedJobs);
        result.put("closedJobs", closedJobs);
        result.put("totalApplicants", totalApplicants);
        result.put("averageApplicantsPerJob", totalJobs == 0 ? 0 : totalApplicants * 1.0 / totalJobs);
        result.put("jobAnalytics", jobAnalytics);

        return result;
    }

    @Override
    public Map<String, Object> moderateJob(Long adminId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        List<String> flags = new ArrayList<>();
        int riskScore = 0;

        if (safe(job.getTitle()).isBlank()) {
            flags.add("Missing job title");
            riskScore += 20;
        }

        if (safe(job.getDescription()).length() < 50) {
            flags.add("Job description is too short");
            riskScore += 20;
        }

        if (safe(job.getRequirements()).length() < 30) {
            flags.add("Job requirements are missing or too short");
            riskScore += 20;
        }

        if (safe(job.getLocation()).isBlank()) {
            flags.add("Missing job location");
            riskScore += 10;
        }

        Optional<EmployerProfile> employerProfile =
                employerProfileRepository.findByUserId(job.getEmployerId());

        if (employerProfile.isEmpty()) {
            flags.add("Missing company profile");
            riskScore += 25;
        } else {
            EmployerProfile profile = employerProfile.get();

            if (safe(profile.getCompanyName()).isBlank()) {
                flags.add("Missing company name");
                riskScore += 20;
            }

            if (!"approved".equals(profile.getVerificationStatus())) {
                flags.add("Company is not verified");
                riskScore += 15;
            }
        }

        String jobText = (
                safe(job.getTitle()) + " " +
                        safe(job.getDescription()) + " " +
                        safe(job.getRequirements())
        ).toLowerCase();

        if (containsSuspiciousWords(jobText)) {
            flags.add("Suspicious words detected");
            riskScore += 30;
        }

        riskScore = Math.min(riskScore, 100);

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("title", job.getTitle());
        result.put("riskScore", riskScore);
        result.put("riskLevel", getRiskLevel(riskScore));
        result.put("flags", flags);
        result.put("recommendation", riskScore >= 60 ? "Manual review required" : "Looks acceptable");

        return result;
    }

    @Override
    public Map<String, Object> detectFraud(Long adminId) {
        List<User> users = userRepository.findAll();
        List<Job> jobs = jobRepository.findAll();

        List<Map<String, Object>> riskyUsers = new ArrayList<>();
        List<Map<String, Object>> riskyJobs = new ArrayList<>();

        for (User user : users) {
            int riskScore = 0;
            List<String> reasons = new ArrayList<>();

            if (!"active".equals(user.getAccountStatus())) {
                riskScore += 25;
                reasons.add("Account is not active");
            }

            if (safe(user.getEmail()).contains("test") || safe(user.getEmail()).contains("spam")) {
                riskScore += 30;
                reasons.add("Suspicious email pattern");
            }

            if ("employer".equals(user.getRole())) {
                Optional<EmployerProfile> profile = employerProfileRepository.findByUserId(user.getId());

                if (profile.isEmpty()) {
                    riskScore += 30;
                    reasons.add("Employer has no company profile");
                } else if (!"approved".equals(profile.get().getVerificationStatus())) {
                    riskScore += 20;
                    reasons.add("Employer profile is not verified");
                }
            }

            if (riskScore > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", user.getId());
                item.put("name", user.getFullName());
                item.put("email", user.getEmail());
                item.put("role", user.getRole());
                item.put("riskScore", riskScore);
                item.put("riskLevel", getRiskLevel(riskScore));
                item.put("reasons", reasons);
                riskyUsers.add(item);
            }
        }

        for (Job job : jobs) {
            Map<String, Object> moderation = moderateJob(adminId, job.getId());
            Integer riskScore = (Integer) moderation.get("riskScore");

            if (riskScore >= 40) {
                riskyJobs.add(moderation);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("riskyUsers", riskyUsers);
        result.put("riskyJobs", riskyJobs);
        result.put("totalRiskyUsers", riskyUsers.size());
        result.put("totalRiskyJobs", riskyJobs.size());

        return result;
    }

    @Override
    public Map<String, Object> getPlatformAnalytics(Long adminId) {
        List<Job> jobs = jobRepository.findAll();
        List<StudentProfile> students = studentProfileRepository.findAll();
        List<Application> applications = applicationRepository.findAll();

        Map<String, Integer> industryMap = new HashMap<>();
        Map<String, Integer> jobTypeMap = new HashMap<>();
        Map<String, Integer> applicationStatusMap = new HashMap<>();

        for (Job job : jobs) {
            String field = safe(job.getFieldOfStudy());
            if (!field.isBlank()) {
                industryMap.put(field, industryMap.getOrDefault(field, 0) + 1);
            }

            String type = safe(job.getEmploymentType());
            if (!type.isBlank()) {
                jobTypeMap.put(type, jobTypeMap.getOrDefault(type, 0) + 1);
            }
        }

        for (Application application : applications) {
            String status = safe(application.getStatus());
            applicationStatusMap.put(status, applicationStatusMap.getOrDefault(status, 0) + 1);
        }

        long successfulApplications = applications.stream()
                .filter(a -> "accepted".equalsIgnoreCase(a.getStatus())
                        || "hired".equalsIgnoreCase(a.getStatus()))
                .count();

        double successRate = applications.isEmpty()
                ? 0
                : successfulApplications * 100.0 / applications.size();

        Map<String, Object> result = new HashMap<>();
        result.put("totalJobs", jobs.size());
        result.put("approvedJobs", jobRepository.countByStatus("approved"));
        result.put("pendingJobs", jobRepository.countByStatus("pending"));
        result.put("closedJobs", jobRepository.countByStatus("closed"));
        result.put("totalStudents", students.size());
        result.put("totalApplications", applications.size());
        result.put("applicationSuccessRate", Math.round(successRate * 100.0) / 100.0);
        result.put("popularIndustries", industryMap);
        result.put("employmentTypeTrends", jobTypeMap);
        result.put("applicationStatusTrends", applicationStatusMap);

        return result;
    }

    @Override
    public Map<String, Object> generateMonthlyReport(Long adminId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Job> monthlyJobs = jobRepository.findByCreatedAtBetween(start, end);
        List<Application> applications = applicationRepository.findAll();

        long monthlyApplications = applications.stream()
                .filter(a -> a.getAppliedAt() != null
                        && !a.getAppliedAt().isBefore(start)
                        && a.getAppliedAt().isBefore(end))
                .count();

        Map<String, Object> analytics = getPlatformAnalytics(adminId);

        String report =
                "Monthly Platform Report - " + currentMonth + "\n\n" +
                        "New jobs this month: " + monthlyJobs.size() + "\n" +
                        "Applications this month: " + monthlyApplications + "\n" +
                        "Total jobs: " + analytics.get("totalJobs") + "\n" +
                        "Total students: " + analytics.get("totalStudents") + "\n" +
                        "Total applications: " + analytics.get("totalApplications") + "\n" +
                        "Application success rate: " + analytics.get("applicationSuccessRate") + "%\n\n" +
                        "AI Insight: The platform shows active hiring activity. " +
                        "Popular industries and employment type trends can be used to guide students and employers.";

        Map<String, Object> result = new HashMap<>();
        result.put("month", currentMonth.toString());
        result.put("newJobs", monthlyJobs.size());
        result.put("monthlyApplications", monthlyApplications);
        result.put("platformAnalytics", analytics);
        result.put("reportContent", report);

        return result;
    }

    private Map<String, Object> buildJobFitAnalysis(StudentProfile student, Job job) {
        int score = 0;
        List<String> whyMatch = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        String studentSkills = safe(student.getSkills()).toLowerCase();
        String studentMajor = safe(student.getMajor()).toLowerCase();
        String preferredLocation = safe(student.getPreferredLocation()).toLowerCase();
        String preferredJobType = safe(student.getPreferredJobType()).toLowerCase();
        String studentBio = safe(student.getBio());

        String jobRequirements = safe(job.getRequirements()).toLowerCase();
        String jobDescription = safe(job.getDescription()).toLowerCase();
        String jobField = safe(job.getFieldOfStudy()).toLowerCase();
        String jobLocation = safe(job.getLocation()).toLowerCase();
        String jobType = safe(job.getEmploymentType()).toLowerCase();

        int skillScore = calculateSkillScore(studentSkills, jobRequirements + " " + jobDescription);
        score += skillScore;

        if (skillScore >= 30) {
            whyMatch.add("Your skills strongly match this job requirement.");
        } else if (skillScore > 0) {
            whyMatch.add("Some of your skills match this job.");
            suggestions.add("Add more job-related skills to improve your match score.");
        } else {
            suggestions.add("Add relevant technical skills required by this job.");
        }

        if (!studentMajor.isBlank()
                && !jobField.isBlank()
                && (jobField.contains(studentMajor) || studentMajor.contains(jobField))) {
            score += 25;
            whyMatch.add("Your major is related to this job field.");
        } else {
            suggestions.add("Highlight coursework, projects, or experience related to this job field.");
        }

        if (!preferredLocation.isBlank()
                && !jobLocation.isBlank()
                && jobLocation.contains(preferredLocation)) {
            score += 15;
            whyMatch.add("The job location matches your preferred location.");
        }

        if (!preferredJobType.isBlank()
                && !jobType.isBlank()
                && jobType.contains(preferredJobType)) {
            score += 10;
            whyMatch.add("The employment type matches your preference.");
        }

        if (!studentBio.isBlank() && studentBio.length() >= 80) {
            score += 10;
            whyMatch.add("Your profile bio is detailed.");
        } else {
            suggestions.add("Write a more detailed bio including projects, experience and career goals.");
        }

        score = Math.min(score, 100);

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("jobTitle", job.getTitle());
        result.put("matchScore", score);
        result.put("matchLevel", getMatchLevel(score));
        result.put("whyMatch", whyMatch);
        result.put("suggestions", suggestions);
        result.put("successProbability", calculateSuccessProbability(score, student));

        return result;
    }

    private int calculateSkillScore(String studentSkills, String jobText) {
        if (studentSkills.isBlank() || jobText.isBlank()) {
            return 0;
        }

        String[] skills = studentSkills.split("[,;\\s]+");
        int matched = 0;

        for (String skill : skills) {
            String s = skill.trim().toLowerCase();

            if (s.length() >= 2 && jobText.contains(s)) {
                matched++;
            }
        }

        return Math.min(matched * 10, 40);
    }

    private int calculateSuccessProbability(int matchScore, StudentProfile student) {
        int probability = matchScore;

        if (!safe(student.getBio()).isBlank()) {
            probability += 5;
        }

        if (!safe(student.getSkills()).isBlank()) {
            probability += 5;
        }

        return Math.min(probability, 95);
    }

    private List<String> buildResumeSuggestions(StudentProfile student) {
        List<String> suggestions = new ArrayList<>();

        if (safe(student.getSkills()).isBlank()) {
            suggestions.add("Add technical and soft skills to your profile.");
        }

        if (safe(student.getBio()).isBlank() || student.getBio().length() < 80) {
            suggestions.add("Write a longer bio including your projects, experience and career goals.");
        }

        if (safe(student.getPreferredLocation()).isBlank()) {
            suggestions.add("Add your preferred work location.");
        }

        if (safe(student.getPreferredJobType()).isBlank()) {
            suggestions.add("Add your preferred job type, such as internship, part-time or full-time.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Your profile looks good. Keep adding new projects and achievements.");
        }

        return suggestions;
    }

    private int calculateBestFitCandidateCount(Job job, List<Application> applications) {
        int count = 0;

        for (Application application : applications) {
            Optional<StudentProfile> studentOptional =
                    studentProfileRepository.findByUserId(application.getStudentId());

            if (studentOptional.isPresent()) {
                Map<String, Object> analysis = buildJobFitAnalysis(studentOptional.get(), job);
                Integer score = (Integer) analysis.get("matchScore");

                if (score >= 70) {
                    count++;
                }
            }
        }

        return count;
    }

    private List<String> extractJobKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String lower = safe(text).toLowerCase();

        String[] possibleKeywords = {
                "java", "spring", "react", "javascript", "python", "sql",
                "communication", "teamwork", "web", "backend", "frontend",
                "data", "marketing", "design", "project"
        };

        for (String keyword : possibleKeywords) {
            if (lower.contains(keyword)) {
                keywords.add(keyword);
            }
        }

        if (keywords.isEmpty()) {
            keywords.add("communication");
            keywords.add("teamwork");
            keywords.add("problem solving");
        }

        return keywords;
    }

    private boolean containsSuspiciousWords(String text) {
        String lower = safe(text).toLowerCase();

        return lower.contains("quick money")
                || lower.contains("no contract")
                || lower.contains("pay first")
                || lower.contains("investment required")
                || lower.contains("guaranteed income")
                || lower.contains("work without visa");
    }

    private StudentProfile getStudentProfileByUserId(Long studentUserId) {
        return studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
    }

    private Job getApprovedJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!"approved".equals(job.getStatus())) {
            throw new RuntimeException("This job is not available");
        }

        return job;
    }

    private Job getEmployerJob(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("You are not allowed to access this job");
        }

        return job;
    }

    private String getMatchLevel(int score) {
        if (score >= 80) {
            return "Excellent Match";
        }

        if (score >= 60) {
            return "Good Match";
        }

        if (score >= 40) {
            return "Fair Match";
        }

        return "Low Match";
    }

    private String getRiskLevel(int score) {
        if (score >= 70) {
            return "High";
        }

        if (score >= 40) {
            return "Medium";
        }

        return "Low";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}