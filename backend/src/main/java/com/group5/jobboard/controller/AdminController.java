package com.group5.jobboard.controller;

import com.group5.jobboard.common.ApiResponse;
import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.JobStatusUpdateRequest;
import com.group5.jobboard.dto.UserStatusUpdateRequest;
import com.group5.jobboard.service.AdminService;
import com.group5.jobboard.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AnalyticsService analyticsService;
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AnalyticsService analyticsService,
                           AdminService adminService,
                           JwtUtil jwtUtil) {
        this.analyticsService = analyticsService;
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(analyticsService.getDashboardSummary());
    }

    @GetMapping("/dashboard/jobs")
    public ApiResponse<Map<String, Object>> getJobStatistics(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(analyticsService.getJobStatistics());
    }

    @GetMapping("/dashboard/applications")
    public ApiResponse<Map<String, Object>> getApplicationStatistics(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(analyticsService.getApplicationStatistics());
    }

    @GetMapping("/dashboard/users")
    public ApiResponse<Map<String, Object>> getUserStatistics(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(analyticsService.getUserStatistics());
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> getUsers(@RequestParam(required = false) String role,
                                                           @RequestParam(required = false) String keyword,
                                                           HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getUsers(role, keyword));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> getUserDetail(@PathVariable Long id,
                                                          HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getUserDetail(id));
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<Map<String, Object>> updateUserStatus(@PathVariable Long id,
                                                             @Valid @RequestBody UserStatusUpdateRequest body,
                                                             HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        return ApiResponse.success("user status updated",
                adminService.updateUserStatus(adminId, id, body.getStatus()));
    }

    @GetMapping("/jobs")
    public ApiResponse<List<Map<String, Object>>> getAdminJobs(@RequestParam(required = false) String status,
                                                               @RequestParam(required = false) String keyword,
                                                               HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getAdminJobs(status, keyword));
    }

    @GetMapping("/jobs/{id}")
    public ApiResponse<Map<String, Object>> getAdminJobDetail(@PathVariable Long id,
                                                              HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getAdminJobDetail(id));
    }

    @PatchMapping("/jobs/{id}/status")
    public ApiResponse<Map<String, Object>> updateJobStatus(@PathVariable Long id,
                                                            @Valid @RequestBody JobStatusUpdateRequest body,
                                                            HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        return ApiResponse.success("job status updated",
                adminService.updateJobStatus(adminId, id, body.getStatus(), body.getReason()));
    }

    @DeleteMapping("/jobs/{id}")
    public ApiResponse<Map<String, Object>> removeJob(@PathVariable Long id,
                                                      HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        return ApiResponse.success("job removed", adminService.removeJob(adminId, id));
    }

    @GetMapping("/employers")
    public ApiResponse<List<Map<String, Object>>> getEmployers(@RequestParam(required = false) String industry,
                                                               @RequestParam(required = false) String keyword,
                                                               HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getEmployers(industry, keyword));
    }

    @GetMapping("/employers/{userId}")
    public ApiResponse<Map<String, Object>> getEmployerDetail(@PathVariable Long userId,
                                                              HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getEmployerDetail(userId));
    }

    @GetMapping("/abnormal")
    public ApiResponse<List<Map<String, Object>>> getAbnormalUsers(@RequestParam(required = false) String riskLevel,
                                                                   HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getAbnormalUsers(riskLevel));
    }

    @GetMapping("/abnormal/{userId}")
    public ApiResponse<Map<String, Object>> getAbnormalUserDetail(@PathVariable Long userId,
                                                                  HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getAbnormalUserDetail(userId));
    }

    @GetMapping("/analytics/active-today")
    public ApiResponse<Map<String, Object>> getActiveToday(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getActiveToday());
    }

    @GetMapping("/analytics/hourly-active")
    public ApiResponse<List<Map<String, Object>>> getHourlyActive(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getHourlyActive());
    }

    @GetMapping("/analytics/trend")
    public ApiResponse<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "7") int days,
                                                           HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getTrend(days));
    }

    @GetMapping("/analytics/distribution")
    public ApiResponse<Map<String, Object>> getDistribution(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getDistribution());
    }

    @GetMapping("/analytics/overview")
    public ApiResponse<Map<String, Object>> getAnalyticsOverview(@RequestParam(defaultValue = "7") int days,
                                                                 HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(analyticsService.getAnalyticsOverview(days));
    }

    private Long requireAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String role = jwtUtil.getRole(token);
        Long userId = jwtUtil.getUserId(token);

        if (!"admin".equals(role)) {
            throw new RuntimeException("Only admin can access this API");
        }

        return userId;
    }
}