package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final JwtUtil jwtUtil;

    public AiController(AiService aiService, JwtUtil jwtUtil) {
        this.aiService = aiService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/jobs/{jobId}/fit")
    public ApiResponse<Map<String, Object>> analyseJobFit(
            @PathVariable Long jobId,
            HttpServletRequest request
    ) {
        Long studentUserId = getUserIdByRole(request, "student");

        return ApiResponse.success(
                "AI job fit analysis generated",
                aiService.analyseJobFit(studentUserId, jobId)
        );
    }

    @GetMapping("/jobs/recommendations")
    public ApiResponse<Map<String, Object>> recommendJobs(HttpServletRequest request) {
        Long studentUserId = getUserIdByRole(request, "student");

        return ApiResponse.success(
                "AI job recommendations generated",
                aiService.recommendJobs(studentUserId)
        );
    }

    @GetMapping("/resume-improvement")
    public ApiResponse<List<String>> improveResume(HttpServletRequest request) {
        Long studentUserId = getUserIdByRole(request, "student");

        return ApiResponse.success(
                "AI resume improvement suggestions generated",
                aiService.improveResume(studentUserId)
        );
    }

    @GetMapping("/jobs/alerts")
    public ApiResponse<Map<String, Object>> getJobAlerts(HttpServletRequest request) {
        Long studentUserId = getUserIdByRole(request, "student");

        return ApiResponse.success(
                "AI job alerts generated",
                aiService.getJobAlerts(studentUserId)
        );
    }

    @PostMapping("/student/chat")
    public ApiResponse<Map<String, Object>> chatWithDeepSeek(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        Long studentUserId = getUserIdByRole(request, "student");
        String message = body.get("message");

        return ApiResponse.success(
                "Qwen AI response generated",
                aiService.chatWithQwen(studentUserId, message)
        );
    }

    @GetMapping("/employer/jobs/{jobId}/ranked-candidates")
    public ApiResponse<Map<String, Object>> rankCandidates(
            @PathVariable Long jobId,
            HttpServletRequest request
    ) {
        Long employerId = getUserIdByRole(request, "employer");

        return ApiResponse.success(
                "AI ranked candidates generated",
                aiService.rankCandidates(employerId, jobId)
        );
    }

    @GetMapping("/employer/jobs/{jobId}/recommended-students")
    public ApiResponse<Map<String, Object>> recommendStudents(
            @PathVariable Long jobId,
            HttpServletRequest request
    ) {
        Long employerId = getUserIdByRole(request, "employer");

        return ApiResponse.success(
                "AI recommended students generated",
                aiService.recommendStudents(employerId, jobId)
        );
    }

    @PostMapping("/employer/job-description/improve")
    public ApiResponse<Map<String, Object>> improveJobDescription(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        getUserIdByRole(request, "employer");

        return ApiResponse.success(
                "AI job description improvement generated",
                aiService.improveJobDescription(body)
        );
    }

    @GetMapping("/employer/hiring-analytics")
    public ApiResponse<Map<String, Object>> getHiringAnalytics(HttpServletRequest request) {
        Long employerId = getUserIdByRole(request, "employer");

        return ApiResponse.success(
                "AI hiring analytics generated",
                aiService.getHiringAnalytics(employerId)
        );
    }

    @GetMapping("/admin/jobs/{jobId}/moderation")
    public ApiResponse<Map<String, Object>> moderateJob(
            @PathVariable Long jobId,
            HttpServletRequest request
    ) {
        Long adminId = getUserIdByRole(request, "admin");

        return ApiResponse.success(
                "AI job moderation generated",
                aiService.moderateJob(adminId, jobId)
        );
    }

    @GetMapping("/admin/fraud-detection")
    public ApiResponse<Map<String, Object>> detectFraud(HttpServletRequest request) {
        Long adminId = getUserIdByRole(request, "admin");

        return ApiResponse.success(
                "AI fraud detection generated",
                aiService.detectFraud(adminId)
        );
    }

    @GetMapping("/admin/platform-analytics")
    public ApiResponse<Map<String, Object>> getPlatformAnalytics(HttpServletRequest request) {
        Long adminId = getUserIdByRole(request, "admin");

        return ApiResponse.success(
                "AI platform analytics generated",
                aiService.getPlatformAnalytics(adminId)
        );
    }

    @GetMapping("/admin/monthly-report")
    public ApiResponse<Map<String, Object>> generateMonthlyReport(HttpServletRequest request) {
        Long adminId = getUserIdByRole(request, "admin");

        return ApiResponse.success(
                "AI monthly report generated",
                aiService.generateMonthlyReport(adminId)
        );
    }

    private Long getUserIdByRole(HttpServletRequest request, String requiredRole) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);

        if (!requiredRole.equals(role)) {
            throw new RuntimeException("Only " + requiredRole + " can use this AI function");
        }

        return jwtUtil.getUserId(token);
    }
}