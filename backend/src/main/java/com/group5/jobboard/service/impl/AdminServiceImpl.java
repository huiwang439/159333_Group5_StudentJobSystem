package com.group5.jobboard.service.impl;

import com.group5.jobboard.entity.*;
import com.group5.jobboard.repository.*;
import com.group5.jobboard.service.AdminService;
import com.group5.jobboard.service.NotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final AnalyticsLogRepository analyticsLogRepository;
    private final ReportRepository reportRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository,
                            EmployerProfileRepository employerProfileRepository,
                            StudentProfileRepository studentProfileRepository,
                            JobRepository jobRepository,
                            JobCategoryRepository jobCategoryRepository,
                            AnalyticsLogRepository analyticsLogRepository,
                            ReportRepository reportRepository,
                            ApplicationRepository applicationRepository,
                            NotificationService notificationService,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jobRepository = jobRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.analyticsLogRepository = analyticsLogRepository;
        this.reportRepository = reportRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Map<String, Object>> getUsers(String role, String keyword) {
        List<User> users;

        boolean hasRole = role != null && !role.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasRole && hasKeyword) {
            users = userRepository.findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                    role, keyword, role, keyword
            );
        } else if (hasRole) {
            users = userRepository.findByRole(role);
        } else if (hasKeyword) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        } else {
            users = userRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            result.add(userToAdminMap(user));
        }

        return result;
    }

    @Override
    public Map<String, Object> getUserDetail(Long userId) {
        User user = getUser(userId);
        Map<String, Object> result = userToAdminMap(user);

        if ("student".equals(user.getRole())) {
            studentProfileRepository.findByUserId(userId).ifPresent(profile -> {
                result.put("university", profile.getUniversity());
                result.put("major", profile.getMajor());
                result.put("degreeLevel", profile.getDegreeLevel());
                result.put("graduationYear", profile.getGraduationYear());
                result.put("skills", profile.getSkills());
                result.put("bio", profile.getBio());
                result.put("preferredLocation", profile.getPreferredLocation());
                result.put("preferredJobType", profile.getPreferredJobType());
                result.put("studentType", profile.getStudentType());
            });
        }

        if ("employer".equals(user.getRole())) {
            employerProfileRepository.findByUserId(userId).ifPresent(profile -> {
                result.put("companyName", profile.getCompanyName());
                result.put("industry", profile.getIndustry());
                result.put("companySize", profile.getCompanySize());
                result.put("website", profile.getWebsite());
                result.put("location", profile.getLocation());
                result.put("companyDescription", profile.getCompanyDescription());
                result.put("contactPerson", profile.getContactPerson());
                result.put("contactEmail", profile.getContactEmail());
                result.put("verificationStatus", profile.getVerificationStatus());
                result.put("logoUrl", profile.getLogoUrl());
            });
        }

        return result;
    }

    @Override
    public Map<String, Object> updateUserStatus(Long adminId, Long userId, String status) {
        if (!isValidAccountStatus(status)) {
            throw new RuntimeException("Invalid account status. Use active, banned, or disabled");
        }

        User operator = getUser(adminId);

        if (!"admin".equals(operator.getRole())) {
            throw new RuntimeException("Only admin can update user status");
        }

        User user = getUser(userId);

        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("Admin account cannot be updated here");
        }

        user.setAccountStatus(status);
        User saved = userRepository.save(user);

        log(adminId, "UPDATE_USER_STATUS", "USER", userId);

        notificationService.createNotification(
                userId,
                "ACCOUNT_STATUS",
                "Account status updated",
                "Your account status has been changed to: " + status
        );

        Map<String, Object> result = userToAdminMap(saved);
        result.put("updated", true);
        return result;
    }

    @Override
    public List<Map<String, Object>> getAdminJobs(String status, String keyword) {
        List<Job> jobs;

        boolean hasStatus = status != null && !status.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasStatus && hasKeyword) {
            jobs = jobRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndLocationContainingIgnoreCase(
                    status, keyword, status, keyword
            );
        } else if (hasStatus) {
            jobs = jobRepository.findByStatus(status);
        } else if (hasKeyword) {
            jobs = jobRepository.findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(keyword, keyword);
        } else {
            jobs = jobRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Job job : jobs) {
            result.add(jobToAdminMap(job));
        }

        return result;
    }

    @Override
    public Map<String, Object> getAdminJobDetail(Long jobId) {
        Job job = getJob(jobId);
        return jobToAdminMap(job);
    }

    @Override
    public Map<String, Object> updateJobStatus(Long adminId, Long jobId, String status, String reason) {
        if (!isValidJobStatus(status)) {
            throw new RuntimeException("Invalid job status. Use pending, approved, rejected, removed, or closed");
        }

        User operator = getUser(adminId);

        if (!"admin".equals(operator.getRole()) && !"staff".equals(operator.getRole())) {
            throw new RuntimeException("Only admin or staff can update job status");
        }

        Job job = getJob(jobId);
        job.setStatus(status);
        Job saved = jobRepository.save(job);

        log(adminId, "UPDATE_JOB_STATUS", "JOB", jobId);

        notificationService.createNotification(
                job.getEmployerId(),
                "JOB_REVIEW",
                "Job review result",
                "Your job [" + job.getTitle() + "] status has been changed to: " + status
                        + (reason == null || reason.isBlank() ? "" : ". Reason: " + reason)
        );

        Map<String, Object> result = jobToAdminMap(saved);
        result.put("updated", true);
        return result;
    }

    @Override
    public Map<String, Object> removeJob(Long adminId, Long jobId) {
        User operator = getUser(adminId);

        if (!"admin".equals(operator.getRole())) {
            throw new RuntimeException("Only admin can remove job");
        }

        Job job = getJob(jobId);
        job.setStatus("removed");
        Job saved = jobRepository.save(job);

        log(adminId, "REMOVE_JOB", "JOB", jobId);

        notificationService.createNotification(
                job.getEmployerId(),
                "JOB_REMOVED",
                "Job removed",
                "Your job [" + job.getTitle() + "] has been removed by admin."
        );

        Map<String, Object> result = jobToAdminMap(saved);
        result.put("removed", true);
        return result;
    }

    @Override
    public List<Map<String, Object>> getEmployers(String industry, String keyword) {
        List<EmployerProfile> profiles;

        boolean hasIndustry = industry != null && !industry.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasIndustry && hasKeyword) {
            profiles = employerProfileRepository.findByIndustryAndCompanyNameContainingIgnoreCaseOrIndustryAndLocationContainingIgnoreCase(
                    industry, keyword, industry, keyword
            );
        } else if (hasIndustry) {
            profiles = employerProfileRepository.findByIndustry(industry);
        } else if (hasKeyword) {
            profiles = employerProfileRepository.findByCompanyNameContainingIgnoreCaseOrIndustryContainingIgnoreCaseOrLocationContainingIgnoreCase(
                    keyword, keyword, keyword
            );
        } else {
            profiles = employerProfileRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (EmployerProfile profile : profiles) {
            result.add(employerToAdminMap(profile));
        }

        return result;
    }

    @Override
    public Map<String, Object> getEmployerDetail(Long userId) {
        EmployerProfile profile = employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        return employerToAdminMap(profile);
    }

    @Override
    public List<Map<String, Object>> getAbnormalUsers(String riskLevel) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> risk = calculateRisk(user, start, end);

            if (riskLevel == null || riskLevel.isBlank() || riskLevel.equals(risk.get("riskLevel"))) {
                if (!"low".equals(risk.get("riskLevel"))) {
                    result.add(risk);
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getAbnormalUserDetail(Long userId) {
        User user = getUser(userId);

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Map<String, Object> result = calculateRisk(user, start, end);
        result.put("logs", analyticsLogRepository.findByUserIdAndCreatedAtBetween(userId, start, end));
        result.put("reportsMadeToday", reportRepository.findByReporterUserIdAndCreatedAtBetween(userId, start, end).size());

        return result;
    }

    @Override
    public Map<String, Object> getActiveToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<AnalyticsLog> logs = analyticsLogRepository.findByCreatedAtBetween(start, end);

        Set<Long> activeUsers = new HashSet<>();
        Set<Long> activeStudents = new HashSet<>();
        Set<Long> activeEmployers = new HashSet<>();
        Set<Long> activeStaff = new HashSet<>();

        for (AnalyticsLog log : logs) {
            if (log.getUserId() == null) {
                continue;
            }

            userRepository.findById(log.getUserId()).ifPresent(user -> {
                if ("student".equals(user.getRole())) {
                    activeUsers.add(user.getId());
                    activeStudents.add(user.getId());
                }

                if ("employer".equals(user.getRole())) {
                    activeUsers.add(user.getId());
                    activeEmployers.add(user.getId());
                }

                if ("staff".equals(user.getRole())) {
                    activeUsers.add(user.getId());
                    activeStaff.add(user.getId());
                }
            });
        }

        Map<String, Object> result = new HashMap<>();
        result.put("date", LocalDate.now().toString());
        result.put("activeUsers", activeUsers.size());
        result.put("activeStudents", activeStudents.size());
        result.put("activeEmployers", activeEmployers.size());
        result.put("activeStaff", activeStaff.size());

        return result;
    }

    @Override
    public List<Map<String, Object>> getHourlyActive() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime start = today.atTime(hour, 0);
            LocalDateTime end = start.plusHours(1);

            List<AnalyticsLog> logs = analyticsLogRepository.findByCreatedAtBetween(start, end);

            Set<Long> users = new HashSet<>();
            int businessLogCount = 0;

            for (AnalyticsLog log : logs) {
                if (log.getUserId() == null) {
                    continue;
                }

                Optional<User> optionalUser = userRepository.findById(log.getUserId());

                if (optionalUser.isPresent() && isBusinessUser(optionalUser.get())) {
                    users.add(log.getUserId());
                    businessLogCount++;
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("hour", hour);
            item.put("activeUsers", users.size());
            item.put("logCount", businessLogCount);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTrend(int days) {
        if (days <= 0) {
            days = 7;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("logs", analyticsLogRepository.findByCreatedAtBetween(start, end).size());
            item.put("applications", applicationRepository.findAll().stream()
                    .filter(a -> !a.getAppliedAt().isBefore(start) && a.getAppliedAt().isBefore(end))
                    .count());
            item.put("jobs", jobRepository.findAll().stream()
                    .filter(j -> !j.getCreatedAt().isBefore(start) && j.getCreatedAt().isBefore(end))
                    .count());

            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> getDistribution() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> users = new HashMap<>();
        users.put("student", userRepository.findByRole("student").size());
        users.put("employer", userRepository.findByRole("employer").size());
        users.put("admin", userRepository.findByRole("admin").size());
        users.put("staff", userRepository.findByRole("staff").size());

        Map<String, Object> jobs = new HashMap<>();
        jobs.put("pending", jobRepository.countByStatus("pending"));
        jobs.put("approved", jobRepository.countByStatus("approved"));
        jobs.put("rejected", jobRepository.countByStatus("rejected"));
        jobs.put("removed", jobRepository.countByStatus("removed"));
        jobs.put("closed", jobRepository.countByStatus("closed"));

        result.put("userRoleDistribution", users);
        result.put("jobStatusDistribution", jobs);

        return result;
    }

    @Override
    public Map<String, Object> createStaff(Long adminId,
                                           String fullName,
                                           String email,
                                           String password,
                                           String phone) {
        User admin = getUser(adminId);

        if (!"admin".equals(admin.getRole())) {
            throw new RuntimeException("Only admin can create staff");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("fullName is required");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("email is required");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("password is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User staff = new User();
        staff.setFullName(fullName);
        staff.setEmail(email);
        staff.setPasswordHash(passwordEncoder.encode(password));
        staff.setRole("staff");
        staff.setPhone(phone);
        staff.setAccountStatus("active");

        User saved = userRepository.save(staff);

        log(adminId, "CREATE_STAFF", "USER", saved.getId());

        Map<String, Object> result = userToAdminMap(saved);
        result.put("created", true);

        return result;
    }

    @Override
    public List<Map<String, Object>> getStaffUsers() {
        List<User> staffUsers = userRepository.findByRole("staff");

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : staffUsers) {
            result.add(userToAdminMap(user));
        }

        return result;
    }

    @Override
    public Map<String, Object> updateStaff(Long adminId,
                                           Long staffId,
                                           String fullName,
                                           String phone,
                                           String status) {
        User admin = getUser(adminId);

        if (!"admin".equals(admin.getRole())) {
            throw new RuntimeException("Only admin can update staff");
        }

        User staff = getUser(staffId);

        if (!"staff".equals(staff.getRole())) {
            throw new RuntimeException("Target user is not staff");
        }

        if (fullName != null && !fullName.isBlank()) {
            staff.setFullName(fullName);
        }

        if (phone != null) {
            staff.setPhone(phone);
        }

        if (status != null && !status.isBlank()) {
            if (!isValidAccountStatus(status)) {
                throw new RuntimeException("Invalid account status. Use active, banned, or disabled");
            }

            staff.setAccountStatus(status);
        }

        User saved = userRepository.save(staff);

        log(adminId, "UPDATE_STAFF", "USER", saved.getId());

        Map<String, Object> result = userToAdminMap(saved);
        result.put("updated", true);

        return result;
    }

    private boolean isBusinessUser(User user) {
        return "student".equals(user.getRole())
                || "employer".equals(user.getRole())
                || "staff".equals(user.getRole());
    }

    private Map<String, Object> userToAdminMap(User user) {
        Map<String, Object> item = new HashMap<>();
        item.put("userId", user.getId());
        item.put("fullName", user.getFullName());
        item.put("email", user.getEmail());
        item.put("role", user.getRole());
        item.put("phone", user.getPhone());
        item.put("accountStatus", user.getAccountStatus());
        item.put("createdAt", user.getCreatedAt());
        item.put("updatedAt", user.getUpdatedAt());

        if ("employer".equals(user.getRole())) {
            employerProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                item.put("companyName", profile.getCompanyName());
                item.put("industry", profile.getIndustry());
                item.put("verificationStatus", profile.getVerificationStatus());
                item.put("logoUrl", profile.getLogoUrl());
            });
        }

        if ("student".equals(user.getRole())) {
            studentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                item.put("university", profile.getUniversity());
                item.put("major", profile.getMajor());
                item.put("degreeLevel", profile.getDegreeLevel());
                item.put("graduationYear", profile.getGraduationYear());
                item.put("studentType", profile.getStudentType());
            });
        }

        return item;
    }

    private Map<String, Object> jobToAdminMap(Job job) {
        Map<String, Object> item = new HashMap<>();
        item.put("jobId", job.getId());
        item.put("employerId", job.getEmployerId());
        item.put("title", job.getTitle());
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
        item.put("targetStudentType", job.getTargetStudentType());
        item.put("createdAt", job.getCreatedAt());
        item.put("updatedAt", job.getUpdatedAt());

        employerProfileRepository.findByUserId(job.getEmployerId())
                .ifPresent(profile -> {
                    item.put("companyName", profile.getCompanyName());
                    item.put("companyLogoUrl", profile.getLogoUrl());
                });

        if (job.getCategoryId() != null) {
            jobCategoryRepository.findById(job.getCategoryId())
                    .ifPresent(category -> item.put("categoryName", category.getCategoryName()));
        }

        return item;
    }

    private Map<String, Object> employerToAdminMap(EmployerProfile profile) {
        Map<String, Object> item = new HashMap<>();

        item.put("employerProfileId", profile.getId());
        item.put("userId", profile.getUserId());
        item.put("companyName", profile.getCompanyName());
        item.put("industry", profile.getIndustry());
        item.put("companySize", profile.getCompanySize());
        item.put("website", profile.getWebsite());
        item.put("location", profile.getLocation());
        item.put("companyDescription", profile.getCompanyDescription());
        item.put("contactPerson", profile.getContactPerson());
        item.put("contactEmail", profile.getContactEmail());
        item.put("verificationStatus", profile.getVerificationStatus());
        item.put("logoUrl", profile.getLogoUrl());
        item.put("createdAt", profile.getCreatedAt());
        item.put("updatedAt", profile.getUpdatedAt());

        userRepository.findById(profile.getUserId()).ifPresent(user -> {
            item.put("fullName", user.getFullName());
            item.put("email", user.getEmail());
            item.put("phone", user.getPhone());
            item.put("accountStatus", user.getAccountStatus());
        });

        return item;
    }

    private Map<String, Object> calculateRisk(User user, LocalDateTime start, LocalDateTime end) {
        List<AnalyticsLog> logs = analyticsLogRepository.findByUserIdAndCreatedAtBetween(user.getId(), start, end);
        int reportCount = reportRepository.findByReporterUserIdAndCreatedAtBetween(user.getId(), start, end).size();

        String riskLevel = "low";
        List<String> reasons = new ArrayList<>();

        if (logs.size() >= 50) {
            riskLevel = "medium";
            reasons.add("Too many actions today: " + logs.size());
        }

        if (reportCount >= 3) {
            riskLevel = "high";
            reasons.add("Too many reports submitted today: " + reportCount);
        }

        long applicationCount = logs.stream()
                .filter(log -> "SUBMIT_APPLICATION".equals(log.getActionType()))
                .count();

        if (applicationCount >= 10 && !"high".equals(riskLevel)) {
            riskLevel = "medium";
            reasons.add("Too many applications submitted today: " + applicationCount);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("accountStatus", user.getAccountStatus());
        result.put("riskLevel", riskLevel);
        result.put("actionCountToday", logs.size());
        result.put("reportCountToday", reportCount);
        result.put("reasons", reasons);

        return result;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Job getJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    private boolean isValidAccountStatus(String status) {
        return "active".equals(status)
                || "banned".equals(status)
                || "disabled".equals(status);
    }

    private boolean isValidJobStatus(String status) {
        return "pending".equals(status)
                || "approved".equals(status)
                || "rejected".equals(status)
                || "removed".equals(status)
                || "closed".equals(status);
    }

    private void log(Long userId, String actionType, String targetType, Long targetId) {
        AnalyticsLog log = new AnalyticsLog();
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        analyticsLogRepository.save(log);
    }
}